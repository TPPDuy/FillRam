package com.zing.zalo.fillrammemory.controllers
import android.os.*
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.utils.GetMemoryThread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener{

    private lateinit var getMemoryThread: GetMemoryThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_nav.setOnNavigationItemSelectedListener(this)
        loadFragment(GenVarFragment.getInstance(), "GEN_VAR_FRAG")
        /*val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(bottom_nav, navController)*/
    }

    override fun onStart() {
        super.onStart()
        getMemoryThread = GetMemoryThread("THREAD", this)
        getMemoryThread.start()
    }

    override fun onStop() {
        super.onStop()
        getMemoryThread.quitSafely()
    }

    private fun loadFragment(fragment: Fragment, tag: String){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frag_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        lateinit var fragment: Fragment
        when(item.itemId){
            R.id.systemInfoFragment -> {
                fragment = GenVarFragment.getInstance()
                loadFragment(fragment, "GEN_VAR_FRAG")
            }
            R.id.appInfoFragment -> {
                fragment = MemoryInfoFragment.getInstance()
                loadFragment(fragment, "MEM_INFO_FRAG")
            }
        }
        return true
    }
}

