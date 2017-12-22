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
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PostManager extends AppCompatActivity {
    private static final String currentSelectedLink = null;
    int postCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_manager);

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        final EditText editTextDelete = (EditText) findViewById(R.id.editText2);
        Button removeButton = (Button) findViewById(R.id.button3);
        final EditText  editTextSet = (EditText) findViewById(R.id.editText3);
        Button setButton = (Button) findViewById(R.id.button4);
        final TextView textView = (TextView) findViewById(R.id.textView6);

        final String[] listAvailabla = new String[1];
        final List<Link> linkList = new DatabaseLinks(this).read();
        final TextView textView1 = (TextView) findViewById(R.id.textView7);


        if(linkList.size() > 0) {
            List<String> paths =new ArrayList<String>();
            int i=0;
            for(Link obj : linkList) {
                paths.add(obj.link);
                if (i == 0) listAvailabla[0] = obj.link;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(PostManager.this, R.layout.support_simple_spinner_dropdown_item, paths);
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    listAvailabla[0] = parent.getItemAtPosition(position).toString();
                    postCount = new PostsDatabase(PostManager.this).countDB(listAvailabla[0]);
                    Log.d("countDB", Integer.toString(postCount));
                    textView1.setText("Posts available: "+Integer.toString(postCount));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }
        else  {
            String[] string = {"No links available"};
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(PostManager.this, R.layout.support_simple_spinner_dropdown_item, string);
            adapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinner.setAdapter(adapter1);
        }


        final int[] amountRemove = {-1};

        postCount = new PostsDatabase(this).countDB(listAvailabla[0]);
        textView1.setText("Posts available: "+Integer.toString(postCount));
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int linkID = new DatabaseLinks(PostManager.this).getID(listAvailabla[0]);
                if (linkID > 0 && !editTextDelete.getText().toString().isEmpty()) {
                    amountRemove[0] = Integer.parseInt(editTextDelete.getText().toString());
                    List<RssFeedModel> db = new PostsDatabase(PostManager.this).read(listAvailabla[0]);
                    for (RssFeedModel obj : db) {
                        if(amountRemove[0] > 0) {
                            boolean deleteStatus =new PostsDatabase(PostManager.this).delete(obj.id, listAvailabla[0]);
                            if (deleteStatus) {
                                Log.i("postInfo", "deleted succesfully");
                                postCount = new PostsDatabase(PostManager.this).countDB(listAvailabla[0]);
                                textView1.setText("Posts available: "+Integer.toString(postCount));
                            }
                            else {
                                Log.i("postInfo", "deleted unsuccesfully");
                            }
                            amountRemove[0]--;
                        }
                        else {
                            break;
                        }
                    }
                }
                else {
                    editTextDelete.setError("Input a number please");
                }
            }
        });

        textView.setText("Current limit: "+Integer.toString(MainActivity.getMaxPostsAmount()));
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editTextSet.getText().toString().isEmpty()) {
                    MainActivity.setMaxPostsAmount(Integer.parseInt(editTextSet.getText().toString()));
                    try {
                        FileOutputStream fOut = openFileOutput("limit.txt",MODE_PRIVATE);
                        fOut.write(Integer.parseInt(editTextSet.getText().toString()));
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    textView.setText("Current limit: "+Integer.toString(MainActivity.getMaxPostsAmount()));
                }
                else {
                    editTextSet.setError("Input a number please");
                }

            }
        });

    }
}
