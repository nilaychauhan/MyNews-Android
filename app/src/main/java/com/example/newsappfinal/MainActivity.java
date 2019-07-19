package com.example.newsappfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsappfinal.api.ApiClient;
import com.example.newsappfinal.api.ApiInterface;
import com.example.newsappfinal.models.Article;
import com.example.newsappfinal.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{



    public static final String API_KEY = "d049d24dc78b42738596ff1fd231a472";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private TextView topheadlines;
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        topheadlines = findViewById(R.id.topheadlines);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

onLoadingSwipeRefresh("");
    }

    public void LoadJson(final String keyword){


        topheadlines.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language = Utils.getLanguage();


        Call<News> call;
        if(keyword.length() > 0){
            call = apiInterface.getNewsSearch(keyword,language,"publishedAt",API_KEY);
        }else{
            call= apiInterface.getNews(country,API_KEY);
        }


        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
             if(response.isSuccessful() && response.body().getArticle() != null){
                 if(!articles.isEmpty()){
                     articles.clear();
                 }
                 articles= response.body().getArticle();
                 adapter = new Adapter(articles,MainActivity.this);
                 recyclerView.setAdapter(adapter);
                 adapter.notifyDataSetChanged();

                 initListener();

                 topheadlines.setVisibility(View.VISIBLE);
                 swipeRefreshLayout.setRefreshing(false);


             }else {
                 topheadlines.setVisibility(View.INVISIBLE);
                 swipeRefreshLayout.setRefreshing(false);
                 Toast.makeText(MainActivity.this, "No Result!", Toast.LENGTH_SHORT).show();
             }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topheadlines.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }



    private void initListener(){

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img",  article.getUrlToImage());
                intent.putExtra("date",  article.getPublishedAt());
                intent.putExtra("source",  article.getSource().getName());
                intent.putExtra("author",  article.getAuthor());

                Pair<View, String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,
                        pair
                );


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, optionsCompat.toBundle());
                }else {
                    startActivity(intent);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(s.length()>2){
                    onLoadingSwipeRefresh(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });


        searchMenuItem.getIcon().setVisible(false,false);

        return true;
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }

    public void onLoadingSwipeRefresh(final String keyword){

        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );

    }
}
