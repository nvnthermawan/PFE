
package com.tunav.tunavmedi.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import com.tunav.tunavmedi.R;
import com.tunav.tunavmedi.adapter.TasksAdapter;
import com.tunav.tunavmedi.app.TunavMedi;
import com.tunav.tunavmedi.datatype.Task;
import com.tunav.tunavmedi.fragment.dialog.TaskDisplay;
import com.tunav.tunavmedi.fragment.dialog.TaskOptions;
import com.tunav.tunavmedi.service.TasksService;
import com.tunav.tunavmedi.service.TasksService.SelfBinder;

public class TaskListFragment extends ListFragment implements ServiceConnection {

    private static final String tag = "TaskListFragment";

    private TasksService mPresenterService = null;
    private ListView mListView = null;
    private int mStackLevel = 0;
    private final OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v(tag, "onItemLongClick()");
            Log.i(tag, "Item clicked: " + id);

            mStackLevel++;

            Task task = (Task) parent.getItemAtPosition(position);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(TaskDisplay.tag);

            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            assert task != null;
            TaskOptions taskOptions = TaskOptions.newInstance(task.getTitle(), task.getImageName(),
                    task.isUrgent(), task.isDone(), task.getNotification());
            taskOptions.setTargetFragment(thisFragment, REQUESTCODE_TASKOPTIONS);
            ft.add(taskOptions, TaskOptions.tag);
            ft.commit();

            mTasksAdapter.notifyDataSetChanged();
            return true;
        }
    };

    public static final int REQUESTCODE_TASKDISPLAY = 0;
    public static final int REQUESTCODE_TASKOPTIONS = 1;
    private final Fragment thisFragment = this;

    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v(tag, "onListItemClick()");
            Log.i(tag, "Item clicked: " + id);

            mStackLevel++;

            Task task = (Task) parent.getItemAtPosition(position);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(TaskDisplay.tag);

            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            assert task != null;
            TaskDisplay taskDialog = TaskDisplay.newInstance(task.getTitle(), task.getImageName(),
                    task.getDescription(), task.getCreated());
            taskDialog.setTargetFragment(thisFragment, REQUESTCODE_TASKDISPLAY);
            ft.add(taskDialog, TaskDisplay.tag);
            ft.commit();
            mTasksAdapter.notifyDataSetChanged();
        }
    };

    private TasksAdapter mTasksAdapter = null;

    private boolean isBound;

    // Called once the parent Activity and the Fragment's UI have
    // been created.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(tag, "onActivityCreated()");
        // Complete the Fragment initialization Ð particularly anything
        // that requires the parent Activity to be initialized or the
        // Fragment's view to be fully inflated.

        mListView = getListView();
        assert mListView != null;
        mListView.setOnItemClickListener(mOnItemClickListener);
        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(mOnItemLongClickListener);
        setEmptyText(getResources().getString(R.string.task_list_empty));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(tag, "onActivityResult()");

        switch (requestCode) {
            case REQUESTCODE_TASKOPTIONS:
                switch (resultCode) {
                    default:
                        break;
                    case Activity.RESULT_OK:
                        // TODO update task
                        break;
                }
                break;
            case REQUESTCODE_TASKDISPLAY:
                break;
            default:
                break;
        }
    }

    // Called when the Fragment is attached to its parent Activity.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(tag, "onAttach()");
        // Get a reference to the parent Activity.
    }

    // Called to do the initial creation of the Fragment.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(tag, "onCreate()");
        // Initialize the Fragment.

        // for the action bar items
        setHasOptionsMenu(true);
        Intent service = new Intent(getActivity(), TasksService.class);
        getActivity().bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        Log.v(tag, "onCreateOptionsMenu()");
        menuInflater.inflate(R.menu.fragment_tasklist, menu);
    }

    // Called once the Fragment has been created in order for it to
    // create its user interface.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(tag, "onCreateView()");
        // Create, or inflate the Fragment's UI, and return it.
        // If this Fragment has no UI then return null.
        return inflater.inflate(android.R.layout.list_content, container,
                false);
    }

    // Called at the end of the full lifetime.
    @Override
    public void onDestroy() {
        Log.v(tag, "onDestroy()");
        // Clean up any resources including ending threads,
        // closing database connections etc.
        getActivity().unbindService(this);

        super.onDestroy();
    }

    // Called when the Fragment's View has been detached.
    @Override
    public void onDestroyView() {
        Log.v(tag, "onDestroyView()");
        // Clean up resources related to the View.
        mListView = null;
        super.onDestroyView();
    }

    // Called when the Fragment has been detached from its parent Activity.
    @Override
    public void onDetach() {
        Log.v(tag, "onDetach()");
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(tag, "onOptionsItemSelected()");

        SharedPreferences.Editor spEditor = getActivity()
                .getSharedPreferences(TunavMedi.SP_USER_NAME,
                        Context.MODE_PRIVATE).edit();
        boolean state = item.isChecked();

        switch (item.getItemId()) {
            case R.id.tasklist_menu_done:
                spEditor.putBoolean(
                        getActivity().getResources().getString(
                                R.string.sp_tasklist_key_show_done), state);
                spEditor.apply();
                if (state) {
                    item.setTitle(R.string.tasklist_menu_done_title_on);
                } else {
                    item.setTitle(R.string.tasklist_menu_done_title_off);
                }
                item.setChecked(!state);
                return true;
            case R.id.tasklist_menu_location:
                spEditor.putBoolean(
                        getActivity().getResources().getString(
                                R.string.sp_tasklist_key_sort_location), state);
                spEditor.apply();
                if (state) {
                    item.setTitle(R.string.tasklist_menu_location_title_on);
                } else {
                    item.setTitle(R.string.tasklist_menu_location_title_off);
                }
                item.setChecked(!state);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // Called at the end of the active lifetime.
    @Override
    public void onPause() {
        Log.v(tag, "onPause()");
        // Suspend UI updates, threads, or CPU intensive processes
        // that don't need to be updated when the Activity isn't
        // the active foreground activity.
        // Persist all edits or state changes
        // as after this call the process is likely to be killed.
        super.onPause();
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume() {
        super.onResume();
        Log.v(tag, "onResume()");
        // Resume any paused UI updates, threads, or processes required
        // by the Fragment but suspended when it became inactive.
    }

    // Called to save UI state changes at the
    // end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.v(tag, "onSaveInstanceState()");
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate, onCreateView, and
        // onCreateView if the parent Activity is killed and restarted.
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v(tag, "onServiceConnected()");
        SelfBinder binder = (SelfBinder) service;
        mPresenterService = binder.getService();
        if (mPresenterService != null) {
            isBound = true;
            mTasksAdapter = mPresenterService.getAdapter();
            setListAdapter(mTasksAdapter);
            Log.i(tag, "binding succesful!");
        } else {
            isBound = false;
            Log.i(tag, "binding failed!");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPresenterService = null;
        isBound = false;
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        Log.v(tag, "onStart()");
        // Apply any required UI change now that the Fragment is visible.
    }

    // Called at the end of the visible lifetime.
    @Override
    public void onStop() {
        Log.v(tag, "onStop()");
        // Suspend remaining UI updates, threads, or processing
        // that aren't required when the Fragment isn't visible.
        super.onStop();
    }
}