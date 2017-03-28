package io.ckl.articles.modules.main;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.ckl.articles.R;
import io.ckl.articles.models.Articles;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * This is a example presenter.
 *
 * The presenter holds a instance of the View, which is a interface implementation.
 * This view should be set as null whenever the activity reaches onDestroy().
 *
 * The presenter is responsible for the business logic, fetching models and telling the view to update.
 */
public class MainPresenter implements MainInterfaces.Presenter {

    MainInterfaces.View view;

    private Context mainPresenterContext;

    private Realm realm;
    private SharedPreferences sortPref;
    private ArrayList<Articles> arrayArticles = new ArrayList<>();

    public MainPresenter(MainInterfaces.View view) {
        this.view = view;
    }

    // region MainInterfaces.Presenter

    @Override
    public void onCreate() {
        mainPresenterContext = view.getViewContext();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(realmConfiguration);

        sortPref = mainPresenterContext.getSharedPreferences(
                mainPresenterContext.getString(R.string.prefFile), Context.MODE_PRIVATE);

        fillArrays();
    }

    @Override
    public void onArticleListPressed(String sortType, boolean decreasing) {
        sortArticles(sortType, decreasing);
    }

    @Override
    public void onDestroy() {
        realm.close();
        this.view = null;
    }

    // end region


    // region private

    private void fillArrays() {
        arrayArticles = new ArrayList<Articles>(realm.where(Articles.class).findAll());

        sortArticles(
                sortPref.getString(mainPresenterContext.getString(R.string.prefKeySortBy),
                        mainPresenterContext.getString(R.string.menuSortDate)),
                sortPref.getBoolean(mainPresenterContext.getString(R.string.prefKeySortDesc),
                        false));
    }


    private void sortArticles(String sortType, boolean decreasing) {
        Collections.sort(arrayArticles, new Comparator<Articles>() {
            @Override
            public int compare(Articles o1, Articles o2) {

                if (decreasing) {
                    Articles temp = o1;
                    o1 = o2;
                    o2 = temp;
                }

                if (sortType.equals(mainPresenterContext.getString(R.string.menuSortWebsite)))
                {
                    return o1.getWebsite().compareToIgnoreCase(o2.getWebsite());
                }
                else if (sortType.equals(mainPresenterContext.getString(R.string.menuSortLabel)))
                {
                    return o1.getTags().get(0).getLabel().compareTo(o2.getTags().get(0).getLabel());
                }
                else if (sortType.equals(mainPresenterContext.getString(R.string.menuSortTitle)))
                {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
                else if (sortType.equals(mainPresenterContext.getString(R.string.menuSortAuthor)))
                {
                    return o1.getAuthors().compareTo(o2.getAuthors());
                }
                else  // If date, do the invert to get the newest (the higher date) first
                {
                    return o2.getDate().compareTo(o1.getDate());
                }
            }
        });

        SharedPreferences.Editor editor = sortPref.edit();
        editor.putString(mainPresenterContext.getString(R.string.prefKeySortBy), sortType);
        editor.putBoolean(mainPresenterContext.getString(R.string.prefKeySortDesc), decreasing);
        editor.apply();

        view.setMenuTitle(sortType, decreasing);

        view.fillList(arrayArticles);
    }

    // end region
}
