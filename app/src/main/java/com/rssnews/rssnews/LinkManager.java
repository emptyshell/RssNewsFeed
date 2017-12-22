package com.rssnews.rssnews;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class LinkManager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_link);

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button addButton = (Button) findViewById(R.id.button);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Button removeButton = (Button) findViewById(R.id.button2);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = editText.getText().toString();
                if(!string.isEmpty()) {
                    if(addLink(string)) {
                        new PostsDatabase(LinkManager.this, true);
                        Toast.makeText(LinkManager.this, "Added succesfully!", 5000).show();
                    }
                    else {
                        Toast.makeText(LinkManager.this, "Something went wrong!", 5000).show();
                    }
                }
                else {
                    editText.setError("Put the link here!");
                }
            }
        });

        final String[] linkToDelete = new String[1];
        final List<Link> linkList = new DatabaseLinks(this).read();
        if(linkList.size() > 0) {
            List<String> paths =new ArrayList<String>();
            int i=0;
            for(Link obj : linkList) {
                paths.add(obj.link);
                //Log.d("String Link: ", paths[i]);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(LinkManager.this, R.layout.support_simple_spinner_dropdown_item, paths);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    linkToDelete[0] = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }
        else  {
            String[] string = {"No links available"};
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(LinkManager.this, R.layout.support_simple_spinner_dropdown_item, string);
            adapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinner.setAdapter(adapter1);
        }
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!linkToDelete[0].isEmpty()) {
                    if(removeLink(linkToDelete[0])) {
                        new PostsDatabase(LinkManager.this, true);
                        Toast.makeText(LinkManager.this, "Removed succesfully!", 5000).show();
                    }
                    else {
                        Toast.makeText(LinkManager.this, "Something went wrong!", 5000).show();
                    }
                }
                else {
                    Toast.makeText(LinkManager.this, "Selected link is empty!", 5000).show();
                }
            }
        });
    }

    public boolean addLink (String string) {
        boolean status = false;
        DatabaseLinks db = new DatabaseLinks(this);
        status= db.addData(string);
        if(status) db.updateDatabaseVersion();
        return status;
    }

    public boolean removeLink (String string) {
        boolean status = false;
        DatabaseLinks db = new DatabaseLinks(this);
        status= db.delete(db.getID(string));
        return status;
    }
}