package com.veyndan.imageloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String[] IMAGE_URLS = {
            "http://pplware.sapo.pt/wp-content/uploads/2015/08/note5-8.0-720x480.jpg",
            "http://icdn6.digitaltrends.com/image/oneplus_x_gallery-720x720.jpg"
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView picassoImageView = (ImageView) findViewById(R.id.picasso_image);
        final ImageView customImageView = (ImageView) findViewById(R.id.custom_image);

        picasso(this, picassoImageView);
        custom(customImageView);
    }

    private static void picasso(final Context context, final ImageView imageView) {
        Picasso.with(context)
                .load(IMAGE_URLS[0])
//                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
//                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(imageView);
    }

    private static void custom(final ImageView imageView) {
        Single.just(IMAGE_URLS[1])
                .flatMap(URL_TO_BYTE_STREAM)
                .map(BYTE_STREAM_TO_BITMAP)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap);
    }

    private static final Function<String, SingleSource<InputStream>> URL_TO_BYTE_STREAM = url -> {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://example.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        final ImageService imageService = retrofit.create(ImageService.class);

        return imageService.getImage(url)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(response -> Timber.d(response.headers().toString()))
                .map(Response::body)
                .doOnSuccess(responseBody -> responseBody.contentType().type())
                .map(ResponseBody::byteStream);
    };

    private static final Function<InputStream, Bitmap> BYTE_STREAM_TO_BITMAP = BitmapFactory::decodeStream;

    private interface ImageService {

        @GET
        Single<Response<ResponseBody>> getImage(@Url String url);
    }
}
