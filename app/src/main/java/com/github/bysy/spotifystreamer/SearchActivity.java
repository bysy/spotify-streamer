package com.github.bysy.spotifystreamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SearchActivity extends AppCompatActivity {
    private Artist[] mArtists;
    private ArtistAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // TODO: replace mock with live data
        mArtists = mockArtists;
        mAdapter = new ArtistAdapter(this, R.layout.single_artist, mArtists);

        ListView lv = (ListView) findViewById(R.id.artistListView);
        lv.setAdapter(mAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist x = (Artist) parent.getItemAtPosition(position);
                // TODO: build intent and start top songs activity in OnItemClickListener
                Toast t = Toast.makeText(parent.getContext(), x.getName(), Toast.LENGTH_SHORT);
                t.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final Artist[] mockArtists = makeMockArtists();
    private Artist[] makeMockArtists() {
        final int w = 256;
        final int h = 256;
        final Bitmap.Config c = Bitmap.Config.ARGB_8888;
        final int[] colors = {0xff4dd0e1, 0xfffff176, 0xffffa726,
                              0xff4db6ac, 0xfff06292, 0xff90a4ae};
        final String[] names = {"Abc artist", "Def singer-songwriter", "Ghi super-group",
                                "Jkl star", "Mno MC", "Pqr pop star"};
        final int len = Math.min(colors.length, names.length);
        Artist[] artists = new Artist[len];
        for (int i = 0; i<len; ++i) {
            Bitmap b = Bitmap.createBitmap(w, h, c);
            b.eraseColor(colors[i]);
            artists[i] = new Artist(names[i], b);
        }
        return artists;
    }

    class ArtistAdapter extends ArrayAdapter<Artist> {
        private Artist[] mArtists;
        private final int mResource;

        ArtistAdapter(Context context, int resource, Artist[] artists) {
            super(context, resource, artists);
            mResource = resource;
            mArtists = artists;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            if (item==null) {
                LayoutInflater li = getLayoutInflater();
                item = li.inflate(mResource, parent, false);
            }
            Artist artist = getItem(position);
            TextView tv = (TextView) item.findViewById(R.id.artistNameView);
            tv.setText(artist.getName());
            ImageView iv = (ImageView) item.findViewById(R.id.artistImageView);
            iv.setImageBitmap(artist.getImage());
            return item;
        }

    }
}

class Artist {
    private final String name;
    private final Bitmap image;

    public Artist(String name, Bitmap image) {
        this.name = name;
        this.image = image;
    }

    String getName() { return name; }
    Bitmap getImage() { return image; }

}
