package abc.flaq.apps.instastudycategories.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import static abc.flaq.apps.instastudycategories.utils.Constants.BUNDLE_CATEGORY_TAB_NO;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_LIST;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.TAB_ACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.TAB_INACTIVE;

public class CategoryFragment extends Fragment {

    private View rootView;
    private StaggeredGridView gridView;
    private CategoryAdapter categoryAdapter;
    private CrystalPreloader preloader;

    private int tabNo;
    private Boolean isApiWorking = false;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Category> activeCategories = new ArrayList<>();   // FIXME: wywalic jednak! zmieniac adapter przy zmianie tab?
    private ArrayList<Category> inactiveCategories = new ArrayList<>();

    public static CategoryFragment newInstance(int tabNo) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_CATEGORY_TAB_NO, tabNo);
        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        tabNo = args.getInt(BUNDLE_CATEGORY_TAB_NO);
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

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        // Update categories when going back from User or Subcategory activity
        /*if (Utils.isNotEmpty(categoryAdapter) && categoryAdapter.getCount() > 0) {
            categoryAdapter = new CategoryAdapter(getActivity(), Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }*/
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && Utils.isNotEmpty(categoryAdapter)) {
            categoryAdapter.notifyDataSetChanged();
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
                //Utils.showInfo(rootView, "Dodano nową kategorię");
                ((CategoryActivity) getActivity()).changeTab(TAB_INACTIVE);

                categories.add(newCategory);
                Session.getInstance().setCategories(categories);

                inactiveCategories.add(newCategory);

                gridView.post(new Runnable() {
                    @Override
                    public void run() {
                        gridView.smoothScrollToPosition(inactiveCategories.size() - 1);
                    }
                });
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
            } else {
                if (categories.size() == 0) {
                    Snackbar.make(rootView, "Nie znaleziono kategorii", Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.colorError))
                            .setAction("ODŚWIEŻ", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // TODO: odswiez
                                }
                            }).show();
                } else {
                    for (Category category : categories) {
                        if (category.isActive()) {
                            activeCategories.add(category);
                        } else {
                            inactiveCategories.add(category);
                        }
                    }
                }
            }
            if (tabNo == TAB_ACTIVE) {
                categoryAdapter = new CategoryAdapter(getActivity(), activeCategories);
            } else {
                categoryAdapter = new CategoryAdapter(getActivity(), inactiveCategories);
            }
            gridView.setAdapter(categoryAdapter);
        }
    }

}
