package abc.flaq.apps.instastudycategories.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.activity.CategoryActivity;
import abc.flaq.apps.instastudycategories.activity.SubcategoryActivity;
import abc.flaq.apps.instastudycategories.activity.UserActivity;
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.API_ALL_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_LIST;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_NAME;

public class ActiveFragment extends Fragment {

    private View rootView;
    private StaggeredGridView gridView;
    private CategoryAdapter categoryAdapter;
    private CrystalPreloader preloader;

    private int tabNo;
    private ArrayList<Category> categories = new ArrayList<>();
    private Boolean isApiWorking = false;

    public static ActiveFragment newInstance(int tabNo) {
        Bundle args = new Bundle();
        args.putInt("tabNo", tabNo);
        ActiveFragment fragment = new ActiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        tabNo = args.getInt("tabNo");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setBroadcastReceiver();
        rootView = inflater.inflate(R.layout.activity_category, container, false);
        gridView = (StaggeredGridView) rootView.findViewById(R.id.category_grid);
        preloader = (CrystalPreloader) rootView.findViewById(R.id.category_preloader);

        preloader.setVisibility(View.VISIBLE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Category selected = categoryAdapter.getItem(position);
                Intent nextIntent;
                if (selected.isAsSubcategory()) {
                    nextIntent = new Intent(getActivity(), UserActivity.class);
                } else {
                    nextIntent = new Intent(getActivity(), SubcategoryActivity.class);
                }
                nextIntent.putExtra(INTENT_CATEGORY_FOREIGN_ID, selected.getForeignId());
                nextIntent.putExtra(INTENT_CATEGORY_NAME, selected.getName());
                getActivity().startActivity(nextIntent);
            }
        });

        // Load categories from session
        /*if (Utils.isEmpty(Session.getInstance().getCategories()) || Session.getInstance().getCategories().size() == 0) {
            new ProcessCategories().execute();
        } else {
            categoryAdapter = new CategoryAdapter(getActivity(), Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }*/

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        // Update categories when going back from User or Subcategory activity
        if (Utils.isNotEmpty(categoryAdapter) && categoryAdapter.getCount() > 0) {
            categoryAdapter = new CategoryAdapter(getActivity(), Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((CategoryActivity) getActivity()).setMainMenuVisibility(menu);
        menu.findItem(R.id.menu_join).setVisible(false);
        menu.findItem(R.id.menu_leave).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isApiWorking || preloader.isShown()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                showSuggestCategoryDialog();
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_leave:
                // not available from here
                break;
            case R.id.menu_sort:
                // not available from here
                break;
            case R.id.menu_info:
                return getActivity().onOptionsItemSelected(item);
            case R.id.menu_login:
                return getActivity().onOptionsItemSelected(item);
            default:
                break;
        }
        return true;
    }

    private void setBroadcastReceiver() {
        CategoryBroadcastReceiver receiver = new CategoryBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_CATEGORY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    private void showSuggestCategoryDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("Nowa kategoria")
                .content("Zaproponuj nową kategorię")
                .positiveText("Zaproponuj")
                .negativeText("Anuluj")
                .titleColorRes(R.color.colorPrimaryDark)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, 20, R.color.colorError)
                .input("Wpisz nazwę...", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // TODO: sprawdzać, czy różne od nazw wszystkich kategorii
                        if (Utils.isNotEmpty(input) && API_ALL_CATEGORY_NAME.equals(input.toString())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            Utils.showInfo(rootView, "Zaproponowano kategorię: " + dialog.getInputEditText().getText());
                            new ProcessAddCategory().execute(dialog.getInputEditText().getText().toString());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }

    private class ProcessAddCategory extends AsyncTask<String, Void, Boolean> {
        Category newCategory;   // TODO: ogranicz mozliwosc do x-razy

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String categoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                newCategory = Api.addCategory(categoryName);
                if (Utils.isNotEmpty(newCategory)) {
                    result = Boolean.TRUE;
                }
            } catch (JSONException e) {
                Utils.logError(getActivity(), "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(getActivity(), "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                // TODO: idz do nowej kategorii - zakladka nieaktywne
                //Utils.showInfo(rootView, "Dodano nową kategorię");
                //categoryAdapter = new CategoryAdapter(getActivity(), Session.getInstance().getCategories());
                //gridView.setAdapter(categoryAdapter);
                categories.add(newCategory);
                //Session.getInstance().setCategories(categories);
                categoryAdapter.notifyDataSetChanged();
            } else {
                Utils.showError(rootView, "Dodanie nowej kategorii zakończone niepowodzeniem");
            }
        }
    }

    private class CategoryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isApiWorking = false;
            preloader.setVisibility(View.GONE);

            categories = intent.getParcelableArrayListExtra(INTENT_CATEGORY_LIST);
            if (Utils.isEmpty(categories)) {
                categories = new ArrayList<>();
                categoryAdapter = new CategoryAdapter(getActivity(), categories);
            } else {
                ArrayList<Category> tabCategories = new ArrayList<>();
                for (Category category : categories) {
                    if (tabNo == 0) {
                        if (category.isActive()) {
                            tabCategories.add(category);
                        }
                    } else {
                        if (!category.isActive()) {
                            tabCategories.add(category);
                        }
                    }
                }
                categoryAdapter = new CategoryAdapter(getActivity(), tabCategories);
            }
            gridView.setAdapter(categoryAdapter);
        }
    }

}
