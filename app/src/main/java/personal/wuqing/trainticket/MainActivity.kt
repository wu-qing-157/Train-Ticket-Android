package personal.wuqing.trainticket

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.query_ordered.*
import kotlinx.android.synthetic.main.query_ticket.*
import kotlinx.android.synthetic.main.station_select.view.*
import personal.wuqing.trainticket.data.Result
import personal.wuqing.trainticket.network.*
import personal.wuqing.trainticket.ui.*
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var userId = ""

    private fun shortToast(s: CharSequence) = Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()

    companion object {
        const val LOGIN_REQUEST = 1
        const val BUY_TICKET_REQUEST = 2
        const val REFUND_TICKET_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        nav_view.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_image).setOnClickListener {
            launchLogin()
        }

        nav_view.setCheckedItem(R.id.nav_query)

        depart_date.editText!!.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        depart_station.setEndIconOnClickListener(SelectStationOnClickListener)
        arrive_station.setEndIconOnClickListener(SelectStationOnClickListener)
        depart_date.setEndIconOnClickListener {
            val dialog = DatePickerDialog(this)
            dialog.setCancelable(false)
            dialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                depart_date.editText!!.setText(getString(R.string.date_format, year, month + 1, dayOfMonth))
            }
            dialog.show()
        }

        catalog_scroll_view.isHorizontalScrollBarEnabled = false
        listOf(
            Catalog('G', "高铁", true),
            Catalog('D', "动车", true),
            Catalog('Z', "直达", false),
            Catalog('T', "特快", false),
            Catalog('K', "快速", false),
            Catalog('L', "临客", true)
        ).forEach { (catalog, name, default) ->
            val checkbox = CheckBox(this)
            checkbox.text = getString(R.string.catalog_and_name, catalog, name)
            checkbox.tag = catalog
            checkbox.isChecked = default
            checkbox.setOnCheckedChangeListener { _, _ -> reportValidity() }
            catalogs.addView(checkbox)
        }

        listOf(depart_station, arrive_station, depart_date).forEach {
            it.editText!!.afterTextChanged { reportValidity() }
        }

        query_ticket.setOnClickListener {
            (listOf(depart_station, arrive_station, depart_date, query_ticket) + catalogs.children)
                .forEach { it.isEnabled = false }
            query_ticket_loading.visibility = View.VISIBLE
            queryTicketAsync()
        }
    }

    fun launchLogin() = startActivityForResult(Intent(this, LoginActivity::class.java), LOGIN_REQUEST)

    fun updateOrderedTicket() =
        if (userId == "") {
            query_ordered_info.visibility = View.INVISIBLE
            ordered_list.visibility = View.INVISIBLE
            ordered_list.removeAllViews()
        } else {
            Thread {
                when (val result = queryOrdered(userId)) {
                    is Result.Success -> runOnUiThread {
                        query_ordered_info.visibility = View.INVISIBLE
                        ordered_list.visibility = View.VISIBLE
                        ordered_list.removeAllViews()
                        result.data.forEach { inflateOrderedTicket(it, ordered_list) }
                    }
                    is Result.Error -> runOnUiThread {
                        query_ordered_info.visibility = View.VISIBLE
                        ordered_list.visibility = View.INVISIBLE
                        query_ticket_info.text = when (result.exception) {
                            is ConnectException, is SocketException -> getString(R.string.failed_connection_refused)
                            is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                            is SocketSyntaxException -> getString(R.string.failed_bad_return)
                            is QueryOrderedEmptyException -> getString(R.string.failed_query_order_no_match)
                            else -> getString(R.string.failed_unknown, result.exception)
                        }
                    }
                }
            }.start()
        }

    data class Catalog(val tag: Char, val name: String, val default: Boolean)

    private fun reportValidity() {
        val departValidity = depart_station.reportValidity(Regex("""\S{1,20}"""), R.string.invalid_station)
        val arriveValidity = arrive_station.reportValidity(Regex("""\S{1,20}"""), R.string.invalid_station)
        val dateValidity = depart_date.reportValidity(Regex("""\d{4}-\d{2}-\d{2}"""), R.string.invalid_date)
        val catalogValidity = catalogs.children.any { (it as? CheckBox)?.isChecked ?: false }
        query_ticket.isEnabled = departValidity && arriveValidity && dateValidity && catalogValidity
    }

    private fun queryTicketAsync() {
        Thread {
            when (val result = queryTicket(
                depart_station.editText!!.text, arrive_station.editText!!.text, depart_date.editText!!.text,
                catalogs.children.filter { (it as? CheckBox)?.isChecked ?: false }
                    .map { (it as CheckBox).tag as Char }.toList()
            )) {
                is Result.Success -> tickets.post {
                    query_ticket_view_animator.displayedChild = 0
                    tickets.removeAllViews()
                    result.data.forEach { inflateTicketInfoCard(it, tickets) }
                    query_ticket_loading.visibility = View.INVISIBLE
                    (listOf(depart_station, arrive_station, depart_date, query_ticket) + catalogs.children)
                        .forEach { it.isEnabled = true }
                }
                is Result.Error -> query_ticket_view_animator.post {
                    query_ticket_view_animator.displayedChild = 1
                    query_ticket_info.text = when (result.exception) {
                        is ConnectException, is SocketException -> getString(R.string.failed_connection_refused)
                        is SocketTimeoutException -> getString(R.string.failed_connection_timeout)
                        is SocketSyntaxException -> getString(R.string.failed_bad_return)
                        is QuertTicketNoMatchException -> getString(R.string.failed_query_ticket_no_match)
                        else -> getString(R.string.failed_unknown, result.exception)
                    }
                    query_ticket_loading.visibility = View.INVISIBLE
                    (listOf(depart_station, arrive_station, depart_date, query_ticket) + catalogs.children)
                        .forEach { it.isEnabled = true }
                }
            }
        }.start()
    }

    object SelectStationOnClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            v!!.context.alert {
                val scrollView = ScrollView(v.context)
                val view =
                    v.context.getSystemService(LayoutInflater::class.java).inflate(R.layout.station_select, scrollView)
                view.station_keyword.editText!!.afterTextChanged {
                    view.radio_group.removeAllViews()
                    val b1 = RadioButton(v.context)
                    val b2 = RadioButton(v.context)
                    b1.text = it
                    b2.text = Math.random().toString()
                    view.radio_group.addView(b1)
                    view.radio_group.addView(b2)
                }
                setView(view)
                setPositiveButton(R.string.action_ok) { _, _ ->
                    view.radio_group.checkedRadioButtonId.let { id ->
                        (v.parent.parent as TextInputLayout).editText!!.setText(
                            if (id == -1) view.station_keyword.editText!!.text
                            else view.radio_group.findViewById<RadioButton>(id).text
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_query -> {
                view_animator.displayedChild = 0
            }
            R.id.nav_ordered -> {
                if (userId == "") launchLogin()
                view_animator.displayedChild = 1
            }
            R.id.nav_account -> {
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOGIN_REQUEST -> {
                val (userId, displayName) = data?.extras?.get(LoginActivity.LOGIN_RESULT) as? LoginResult
                    ?: return
                this.userId = userId
                shortToast(getString(R.string.welcome).format(displayName))
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_header_title).text =
                    getString(R.string.welcome).format(displayName)
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_header_subtitle).text =
                    getString(R.string.display_user_id).format(userId)
                nav_view.getHeaderView(0).findViewById<ImageView>(R.id.nav_header_image).setOnClickListener(null)
                updateOrderedTicket()
            }
            BUY_TICKET_REQUEST -> updateOrderedTicket()
            REFUND_TICKET_REQUEST -> updateOrderedTicket()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
