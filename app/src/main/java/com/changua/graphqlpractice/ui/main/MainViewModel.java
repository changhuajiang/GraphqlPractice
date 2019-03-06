package com.changua.graphqlpractice.ui.main;


import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.changhua.graphqlpractice.FindQuery;

import android.arch.lifecycle.ViewModel;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.util.Log;
import android.view.View;

import com.apollographql.apollo.ApolloCall;


import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.exception.ApolloException;
import com.changua.graphqlpractice.BuildConfig;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.annotation.Nonnull;


import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    private static final String BASE_URL = "https://api.github.com/graphql";
    private ApolloClient apolloClient;


    public ObservableField<String> repoName = new ObservableField("");
    public ObservableField<String> ownerName = new ObservableField("");

    public ObservableField<String> name = new ObservableField("");
    public ObservableField<String> url = new ObservableField("");
    public ObservableField<String> description = new ObservableField("");
    public ObservableField<String> forkCount = new ObservableField("");
    public ObservableField<Boolean> isLoading = new ObservableField(false);

    public void init() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(new Interceptor() {

                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .method(original.method(), original.body())
                                .addHeader("Authorization",
                                        "Bearer " + BuildConfig.AUTH_TOKEN)
                                .build();

                        return chain.proceed(request);
                    }
                })
                .build();

        apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                //.normalizedCache(normalizedCacheFactory, cacheKeyResolver)
                .build();
    }


    public void queryRepo() {

        isLoading.set( true);
        apolloClient.query(FindQuery.builder()
                .name(repoName.get()) //Passing required arguments
                .owner(ownerName.get()) //Passing required arguments
                .build())
                .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
                .enqueue(new ApolloCall.Callback<FindQuery.Data>() {

                    @Override
                    public void onResponse(@NotNull com.apollographql.apollo.api.Response<FindQuery.Data> response) {

                        isLoading.set( false);

                        if (response == null ||
                                response.data() == null ||
                                response.data().repository() == null) {

                            return;
                        }

                        Log.d("MainViewModel", response.data().repository().name());
                        name.set(response.data().repository().name());
                        url.set(response.data().repository().url());
                        description.set(response.data().repository().description());
                        int tt = response.data().repository().forkCount();
                        forkCount.set(String.valueOf(tt));
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e("", e.getMessage(), e);
                        isLoading.set( false);
                    }
                });

    }

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}



