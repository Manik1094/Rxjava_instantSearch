package com.example.helloworld.instantsearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.example.helloworld.instantsearch.adapter.ContactsAdapter;
import com.example.helloworld.instantsearch.network.model.ApiClient;
import com.example.helloworld.instantsearch.network.model.ApiService;
import com.example.helloworld.instantsearch.network.model.Contact;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class RemoteSearchActivity extends AppCompatActivity {

    private static final String TAG = RemoteSearchActivity.class.getSimpleName();

    private CompositeDisposable disposable = new CompositeDisposable();
    private PublishSubject<String> publishSubject = PublishSubject.create();
    private ApiService apiService;
    private ContactsAdapter mAdapter;
    private List<Contact> contactsList = new ArrayList<>();

    @BindView(R.id.input_search)
    EditText inputSearch;


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_search);

        unbinder = ButterKnife.bind(this);
        mAdapter = new ContactsAdapter(this, contactsList, new ContactsAdapter.ContactsAdapterListener() {
            @Override
            public void onContactSelected(Contact contact) {

            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this , DividerItemDecoration.VERTICAL );
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        DisposableObserver<List<Contact>> observer = getSearchObserver();

        disposable.add(publishSubject.debounce(300 , TimeUnit.MILLISECONDS)
                     .distinctUntilChanged()
                      .switchMapSingle(new Function<String, Single<List<Contact>>>() {
                          @Override
                          public Single<List<Contact>> apply(String s) throws Exception {
                              return apiService.getContacts(null, s)
                                      .subscribeOn(Schedulers.io())
                                      .observeOn(AndroidSchedulers.mainThread());
                          }
                      })
                  .subscribeWith(observer));

        disposable.add(RxTextView.textChangeEvents(inputSearch)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContactsTextWatcher()));

        disposable.add(observer);

        // passing empty string fetches all the contacts
        publishSubject.onNext("");
    }

    private DisposableObserver<List<Contact>> getSearchObserver() {
        return new DisposableObserver<List<Contact>>() {
            @Override
            public void onNext(List<Contact> contacts) {
                contactsList.clear();
                contactsList.addAll(contacts);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    private DisposableObserver<TextViewTextChangeEvent> searchContactsTextWatcher() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text());
                publishSubject.onNext(textViewTextChangeEvent.text().toString());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }
}
