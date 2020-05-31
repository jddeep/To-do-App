package com.jddeep.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jddeep.todoapp.adapter.TodoAdapter;
import com.jddeep.todoapp.database.DBHelper;
import com.jddeep.todoapp.model.TodoModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TodoAdapter.OnItemDeleteListener {

    private TodoAdapter todoAdapter;
    private DBHelper dbHelper;
    private ArrayList<TodoModel> todoList = new ArrayList<>();

    private String priorityTag;
    private AlertDialog addTaskDialog;
    private static boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        setTitle(getString(R.string.app_name));
        dbHelper = new DBHelper(this);
        loadTodoItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isSearching = false;
                loadTodoItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                ArrayList<TodoModel> newPendingTodoModels = new ArrayList<>();
                for (TodoModel pendingTodoModel : dbHelper.getTodoList()) {
                    String getTodoTitle = Objects.requireNonNull(pendingTodoModel.getTitle()).toLowerCase();
                    String getTodoTag = Objects.requireNonNull(pendingTodoModel.getPriorityTag()).toLowerCase();

                    if ((getTodoTitle.contains(newText) || getTodoTag.contains(newText))) {
                        newPendingTodoModels.add(pendingTodoModel);
                    }
                }
                isSearching = !newText.isEmpty();
                todoList.clear();
                todoList.addAll(newPendingTodoModels);
                loadTodoItems();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                return true;
            case R.id.add_todo:
                showAddTodoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void loadTodoItems() {
        if (!isSearching)
            todoList = dbHelper.getTodoList();
        RecyclerView todoRv = findViewById(R.id.todo_rv);
        LinearLayout noTodoLayout = findViewById(R.id.no_pending_todo_section);

        if (dbHelper.getTodoCount() == 0) {
            todoRv.setVisibility(View.GONE);
            noTodoLayout.setVisibility(View.VISIBLE);
        } else {
            todoRv.setVisibility(View.VISIBLE);
            noTodoLayout.setVisibility(View.GONE);
            todoRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            todoAdapter = new TodoAdapter(todoList, this);
            todoAdapter.setItemDeleteListener(this);
            todoRv.setAdapter(todoAdapter);
        }
    }

    //show add new todos dialog and adding the todos into the database
    private void showAddTodoDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.add_new_todo_dialog, null);
        builder.setView(view);
        final TextInputEditText todoTitle = view.findViewById(R.id.todo_title);
        Spinner todoTags = view.findViewById(R.id.todo_tag);
        //stores all the tags title in string format
        final String[] tags = {"low", "high", "medium"};
        ArrayAdapter<String> tagsModelArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tags);
        tagsModelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //setting the spinner adapter
        todoTags.setAdapter(tagsModelArrayAdapter);
        todoTags.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                priorityTag = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        TextView cancel = view.findViewById(R.id.cancel);
        Button addTodo = view.findViewById(R.id.add_new_todo);
        addTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getting all the values from add new todos dialog
                String getTodoTitle = Objects.requireNonNull(todoTitle.getText()).toString();

                //checking the data fields
                boolean isTitleEmpty = todoTitle.getText().toString().isEmpty();

                //adding the task
                if (isTitleEmpty) {
                    todoTitle.setError("Todo title required !");
                } else {
                    long id = dbHelper.insertTodo(
                            new TodoModel(getTodoTitle, priorityTag)
                    );
                    isSearching = false;
                    loadTodoItems();
                    if (todoAdapter != null)
                        todoAdapter.notifyDataSetChanged();
                    addTaskDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Added Task", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTaskDialog.dismiss();
            }
        });
        addTaskDialog = builder.create();
        addTaskDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemDelete(@NotNull TodoModel todo) {
        loadTodoItems(); //Refresh after delete
    }
}
