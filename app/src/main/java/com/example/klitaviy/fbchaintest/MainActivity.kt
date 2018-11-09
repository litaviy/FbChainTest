package com.example.klitaviy.fbchaintest

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val random = Random()

        sendNotification.setOnClickListener {
            FirebaseKeeper.firebase.handleNotification("Hello_World_${random.nextInt(1000)}")
        }

        val authService = AuthService(FirebaseKeeper.firebase)
        authorizationCall.setOnClickListener {
            authService.authorize("some_creds")
                .subscribeOn(Schedulers.newThread())
                .doOnSubscribe {   Log.d("my_something", "Running auth chain...") }
                .subscribe({
                    Log.d("my_something", "Authorization Complete.")
                }, { error ->
                    Log.d("my_something", "Authorization Error : ${error.message}")
                    error.printStackTrace()
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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
}

class AuthService(private val firebase: Firebase) {

    private val serviceCall = Completable.complete().delay(2, TimeUnit.SECONDS)

    fun authorize(credentials: String): Completable =
        // Call API Here.
        serviceCall.andThen(
            Single.defer { firebase.getPublisher() }
        ).flatMapCompletable { token ->
            // Save Token Here.
            Completable.fromAction {
                Log.d("my_something", "Saving Token : $token")
            }
        }
}

class Firebase {
    private val publisher: PublishSubject<String> = PublishSubject.create()

    fun handleNotification(notification: String) {
        Log.d("my_something", "Sending Notification : $notification")
        publisher.onNext(notification)
    }

    fun getPublisher(): Single<String> = publisher.firstOrError()
}

object FirebaseKeeper {
    val firebase = Firebase()
}
