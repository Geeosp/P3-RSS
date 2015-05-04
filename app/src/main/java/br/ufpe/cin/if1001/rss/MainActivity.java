package br.ufpe.cin.if1001.rss;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openPreferences();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    void openPreferences(){
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }

    public boolean isConnected(){//verifica se o telefone está conectado ou nao
        ConnectivityManager connectivity = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
            if (netInfo == null) {
                return false;
            }
            int netType = netInfo.getType();
            if (netType == ConnectivityManager.TYPE_WIFI ||
                    netType == ConnectivityManager.TYPE_MOBILE) {
                return netInfo.isConnected();
            } else {
                return false;
            }
        }else{
            return false;
        }
    }




    /**
     * A placeholder fragment containing a simple view.
     */





    public class PlaceholderFragment extends Fragment {
        private ListView mRssFeed;
        //   public Context context;
        private String feedurl = null;

        @Override
        public void onStart() {
            super.onStart();
            //pode passar varias URLs, mas so vai pegar a primeira no codigo de doInBackground() abaixo
            SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
            feedurl = sharedPreferences.getString("feed_url", "http://g1.globo.com/dynamo/rss2.xml");

            if(isConnected()){
                new CarregarFeed().execute(feedurl);
            }else{
            loadFromSql();
            }





        }
        public void loadFromSql(){

        }

        private class CarregarFeed extends AsyncTask<String, Void, List<FeedItem>> {

            @Override
            protected List<FeedItem> doInBackground(String... params) {
                String result = "";
                List<FeedItem> items = null;
                InputStream in = null;
                try {
                    result = getRssFeed(params[0]);
                    items = parse(result);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return items;
            }

            private String getRssFeed(String feed) throws IOException {
                InputStream in = null;
                String rssFeed = null;
                try {
                    URL url = new URL(feed);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    in = conn.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int count; (count = in.read(buffer)) != -1; ) {
                        out.write(buffer, 0, count);
                    }
                    byte[] response = out.toByteArray();
                    rssFeed = new String(response, "UTF-8");
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                return rssFeed;
            }


            private List<FeedItem> parse(String rssFeed)
                    throws XmlPullParserException, IOException {

                // pegando instancia da XmlPullParserFactory [singleton]
                XmlPullParserFactory factory = XmlPullParserFactory
                        .newInstance();
                // criando novo objeto do tipo XmlPullParser
                XmlPullParser parser = factory.newPullParser();
                // Definindo a entrada do nosso parser - argumento passado como
                // parametro
                parser.setInput(new StringReader(rssFeed));
                // Definindo retorno
                List<FeedItem> items = new Vector<FeedItem>();
                boolean forever = true;
                FeedItem feedItem = new FeedItem();///inicializa o primeiro item
                int eventType = parser.getEventType();
                String currentTag = null;
                Integer id = null;
                String title = null;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        if ("item".equals(parser.getName())) {//renova o objeto para cada item novo
                            feedItem = new FeedItem();
                        }
                    } else if (eventType == XmlPullParser.TEXT) {

                        if ("title".equals(currentTag)) {//salva o titulo
                            feedItem.title = parser.getText();
                        } else if (currentTag.equals("link")) {//salva o link
                         //   Log.w("tag", "link");
                            feedItem.link = parser.getText();
                        } else if (currentTag.equals("description")) {//salva a descricao
                          //  Log.w("tag", "description");
                            String text = parser.getText();
                            int o = text.lastIndexOf('>')+1;//algumas descricoes vem com um link e uma imagem. To ignorando eles por enquanto
                            text = text.substring(o);
                            feedItem.description = text;
                        //    Log.e("oi", "" + feedItem.description);
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if ("item".equals(parser.getName())) {//salva o itemFeed na lista
                            items.add(feedItem);
                        }
                    }
                    eventType = parser.next();
                }
                return items;
            }

            @Override
            protected void onPreExecute() {
                Toast.makeText(getActivity(), "carregando...", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(List<FeedItem> result) {
                if (result != null) {
                    FeedAdapter adapter = new FeedAdapter(getApplicationContext());
                    adapter.feedItens = result;
                    mRssFeed.setAdapter(adapter);
                    mRssFeed.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //cuidado com esse cast, fazer apenas se tiver certeza do tipo
                            //outra forma, pegando o conteudo exibido no item clicado da list view
                            String link = ((FeedItem)   mRssFeed.getAdapter().getItem(position)).link;
                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            startActivity(myIntent);
                        }

                    });
                }
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mRssFeed = (ListView) rootView.findViewById(R.id.rss_feed);
            return rootView;
        }

        public void saveFeedItem(FeedItem item){
            FeedItem aux = null;
            Cursor cursor = null;
            SQLiteDatabase sqLiteDatabase = new DatabaseHelper(getApplicationContext()).getWritableDatabase();
            String where = "title = ?";
            String colunas[] =new String[] {"TITLE", "DESCRIPTION", "LINK", "LIDO"};
            String argumentos[] = new String[] {"*"};
            cursor = sqLiteDatabase.query("RSS", colunas, where,argumentos, null, null, null);


        }




    }


    public class SettingsFragment extends PreferenceFragment {//Fragment de configuracoes
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.fragment_prefs);

            addPreferencesFromResource(R.xml.preference);

            Preference button = (Preference)findPreference("ok_button");//ação do botao de ok, retorna ao fragment principal
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new PlaceholderFragment())
                            .commit();
                    return true;
                }
            });
        }




    }

    class FeedAdapter extends BaseAdapter {
        Context context;

        List<FeedItem> feedItens;

        public void setFeedList(List<FeedItem> list) {
            this.feedItens = list;
        }

        public FeedAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return feedItens.size();
        }

        @Override
        public FeedItem getItem(int i) {
            return feedItens.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            FeedItemHolder holder;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item_feed, viewGroup, false);
                holder = new FeedItemHolder();
                holder.tv_description = (TextView) view.findViewById(R.id.tv_content);
                holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
                view.setTag(holder);
            } else {
                holder = (FeedItemHolder) view.getTag();
            }
            FeedItem feed = getItem(i);
            holder.tv_title.setText(feed.title);
            holder.tv_description.setText(feed.description);
            return view;
        }
    }

}
