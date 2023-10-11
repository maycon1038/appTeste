package online.pmam.appdeteste;

import static com.msm.themes.util.Util.isOnlineV1;
import static com.pmam.loginsispmam.util.Preferencia.getTokenFCM;
import static com.pmam.loginsispmam.util.Preferencia.setInstallationUUID;
import static com.pmam.loginsispmam.util.Util.checkVersion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;
import com.msm.themes.BaseActivity;
import com.msm.themes.ThemeUtil;
import com.pmam.loginsispmam.checkconta.CheckContaActivity;
import com.pmam.loginsispmam.data.model.mGrupoPerfils;
import com.pmam.loginsispmam.login.LoginActivity;
import com.pmam.loginsispmam.util.Preferencia;
import com.pmam.loginsispmam.util.Tag;
import com.pmam.produtividade.produtividade.ProdutividadeActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import online.pmam.appdeteste.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private final Tag tg = new Tag(getClass().getSimpleName());
    private FirebaseAuth mAuth;
    private Context ctx;
    private FirebaseFirestore dbFirestore;
    private FirebaseCrashlytics mFirebaseAnalytics;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.setMyTheme(this, ThemeUtil.THEME_GREEN_DARK);
        super.onCreate(savedInstanceState);
        ctx = this;
        mFirebaseAnalytics = FirebaseCrashlytics.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        try {
            //      FirebaseFunctions functions = FirebaseFunctions.getInstance();
            //functions.useEmulator("10.0.2.2", 5001);
        /*    mAuth.useEmulator("192.168.1.176", 9099);
            dbFirestore.useEmulator("192.168.1.176", 8080);
            Preferencia.setHost(this,"192.168.1.176","5001");*/
            Preferencia.clearHost(ctx);
            if (isOnlineV1(ctx)) {
                dbFirestore.clearPersistence();
            }
            dbFirestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.e(" Splash ", " " + e.toString());
        }


        FirebaseInstallations.getInstance().getId()
                .addOnSuccessListener(task -> {
                    setInstallationUUID(ctx, task);
                    mFirebaseAnalytics.setUserId(task);
                }).addOnFailureListener(e -> {
                    mFirebaseAnalytics.setUserId(Preferencia.getInstallationUUID(ctx));
                    mFirebaseAnalytics.recordException(e);


                });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



        FirebaseApp.initializeApp(ctx);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());


        tg.LogD(new Exception(), getTokenFCM(ctx));

        if (mAuth.getCurrentUser() != null) {
            ((TextView) findViewById(R.id.user_name)).setText("Usuário logado: " + mAuth.getCurrentUser().getEmail());
        } else {
            ((TextView) findViewById(R.id.user_name)).setText("Ops, faça login!");
        }

        findViewById(R.id.btn_entrar).setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        });

        findViewById(R.id.btn_sair).setOnClickListener(v -> signOut());


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  Snackbar.make(view, "Substitua por sua própria ação", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();*/
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        ThemeUtil.setMode(MainActivity.this, true);
                        recreate();
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        ThemeUtil.setMode(MainActivity.this, false);
                        recreate();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }



    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (checkDadosPessoais()) {
                checkPerfil();
                Intent it = new Intent(ctx, ProdutividadeActivity.class);
                startActivity(it);

                Toast.makeText(ctx, "Usuário logado!", Toast.LENGTH_SHORT).show();
            } else {
                Intent it = new Intent(ctx, CheckContaActivity.class);
                startActivity(it);
            }
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    private boolean checkDadosPessoais() {

        return !Preferencia.getLotacao(ctx).isEmpty() ||
                !Preferencia.getNome(ctx).isEmpty() ||
                !Preferencia.getLotacao(ctx).isEmpty() ||
                !Preferencia.getCPF(ctx).isEmpty() ||
                !Preferencia.getIdDocConvenio(ctx).isEmpty();

    }

    public void checkPerfil() {
        if (dbFirestore != null && !Preferencia.getCPF(ctx).isEmpty()) {

            dbFirestore.collection("user-perfil").document(Preferencia.getCPF(ctx)).collection("grupos").addSnapshotListener((value, error) -> {
                ///SINCRONIZANDO DADOS LOCAL
                if (error == null && value != null) {

                    final List<DocumentChange> docs = value.getDocumentChanges();

                    if (docs.size() > 0) {
                        ArrayList<mGrupoPerfils> listV = new ArrayList<>();
                        for (DocumentChange document : docs) {
                            listV.add(document.getDocument().toObject(mGrupoPerfils.class));

                        }
                        //tg.LogD(new Throwable(), "Perfil atualizado " + listV.toString());
                        setGrupoPerfil(ctx, listV);

                    }

                }


            });

        }
    }

    private void setGrupoPerfil(Context paramContext, ArrayList<mGrupoPerfils> listp) {
        if (listp != null && listp.size() > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(listp);
            SharedPreferences.Editor editor = paramContext.getSharedPreferences("preferencias", 0).edit();
            editor.putString("aListGrupoPerfil", json);
            editor.apply();
        }
    }
}