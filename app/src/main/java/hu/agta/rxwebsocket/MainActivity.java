package hu.agta.rxwebsocket;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RxWebSocket rxWebSocket;
    private RecyclerView messageList;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageList = findViewById(R.id.messageList);
        adapter = new MessageAdapter();
        messageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        messageList.setAdapter(adapter);

        rxWebSocket = new RxWebSocket("ws://44.237.74.211/ws/chat/one-to-one/6/fb9a5773bfe8fa18138852f6896d370dbaa64b39/");

        rxWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketOpenEvent -> {
                    Snackbar.make(messageList, "WebSocket opened.", Snackbar.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);

        rxWebSocket.onClosed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosedEvent -> {
                    Snackbar.make(messageList, "WebSocket closed.", Snackbar.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);

        rxWebSocket.onClosing()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketClosingEvent -> {
                    Snackbar.make(messageList, "WebSocket is closing.", Snackbar.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);

        rxWebSocket.onTextMessage()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketMessageEvent -> {
                    adapter.addMessage(socketMessageEvent);
                }, Throwable::printStackTrace);

        rxWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(socketFailureEvent -> {
                    Snackbar.make(messageList, "WebSocket failure! " + socketFailureEvent.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }, Throwable::printStackTrace);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final EditText textInput = new EditText(MainActivity.this);
            textInput.setHint("Some message...");
            textInput.setPadding(32, 32, 32, 32);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("New message")
                    .setView(textInput)
                    .setPositiveButton("Send", (dialog, which) -> {
                        JSONObject jobj = new JSONObject();
                        String msg = "{\"message\": \"/hug\"}";
                        try {
                            jobj = new JSONObject(msg);
                        } catch (JSONException e) {
                            //e.printStackTrace();
                        }

                        rxWebSocket.sendMessage(jobj)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        success -> Snackbar.make(messageList, "Message sent result: " + success, Snackbar.LENGTH_SHORT).show(),
                                        throwable -> Snackbar.make(messageList, "Message error: " + throwable.getMessage(), Snackbar.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            rxWebSocket.connect();
            return true;
        }
        if (id == R.id.action_disconnect) {
            rxWebSocket.close()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success ->
                                    Snackbar.make(messageList, "WebSocket close initiated. With result: " + success,
                                            Snackbar.LENGTH_SHORT).show()
                            , Throwable::printStackTrace);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
