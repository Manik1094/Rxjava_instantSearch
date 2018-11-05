package com.example.helloworld.instantsearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.example.helloworld.instantsearch.network.model.ApiClient;
import com.example.helloworld.instantsearch.network.model.ApiService;
import com.example.helloworld.instantsearch.network.model.Contact;
import com.example.helloworld.instantsearch.view.ContactsAdapterFilterable;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LocalSearchActivity extends AppCompatActivity {

    private CompositeDisposable disposable = new CompositeDisposable();
    private ContactsAdapterFilterable mAdapter;
    private static final String TAG = LocalSearchActivity.class.getSimpleName();
    private ApiService apiService;
    private List<Contact> contactsList = new ArrayList<>();

    @BindView(R.id.input_search)
    EditText inputSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_search);

        unbinder = ButterKnife.bind(this);
        mAdapter = new ContactsAdapterFilterable(this, contactsList, new ContactsAdapterFilterable.ContactsAdapterListener() {
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

        disposable.add(RxTextView.textChangeEvents(inputSearch)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContacts()));


        // fetching gmail contacts on launching app
        fetchContacts("gmail");

    }
    private DisposableObserver<TextViewTextChangeEvent> searchContacts() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                Log.e(TAG, "Search query: " + textViewTextChangeEvent.text());
                mAdapter.getFilter().filter(textViewTextChangeEvent.text());
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

    private void fetchContacts(String source) {
        disposable.add(apiService
                .getContacts(source, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<Contact>>() {
                    @Override
                    public void onSuccess(List<Contact> contacts) {
                        contactsList.clear();
                        contactsList.addAll(contacts);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        disposable.clear();
        unbinder.unbind();
    }
}
