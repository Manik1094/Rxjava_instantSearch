package com.example.helloworld.instantsearch.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.helloworld.instantsearch.R;
import com.example.helloworld.instantsearch.network.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapterFilterable extends RecyclerView.Adapter<ContactsAdapterFilterable.MyViewHolder> implements Filterable {

    private Context context;
    private List<Contact> contactList;
    private List<Contact> contactListFiltered;
    private ContactsAdapterListener listener;

    public ContactsAdapterFilterable(Context context, List<Contact> contactList, ContactsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.contactList = contactList;
        this.contactListFiltered = contactList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.contact_row_item, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final Contact contact = contactListFiltered.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());

        Glide.with(context)
                .load(contact.getProfileImage())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.thumbnail);

    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
      return new Filter() {
          @Override
          protected FilterResults performFiltering(CharSequence constraint) {

              String charString = constraint.toString();
              if (charString.isEmpty()){

                  contactListFiltered = contactList;
              }else {
                  List<Contact> filteredList = new ArrayList<>();
                  for (Contact row : contactList){

                      if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhone().contains(charString)){
                          filteredList.add(row);
                      }
                  }
                  contactListFiltered = filteredList;
              }
              FilterResults results = new FilterResults();

              results.values = contactListFiltered;
              return results;
          }

          @Override
          protected void publishResults(CharSequence constraint, FilterResults results) {

              contactListFiltered = (ArrayList<Contact>) results.values;
              notifyDataSetChanged();
          }
      };
    }
    public interface ContactsAdapterListener {
        void onContactSelected(Contact contact);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            phone = view.findViewById(R.id.phone);
            thumbnail = view.findViewById(R.id.thumbnail);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onContactSelected(contactListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }
}
