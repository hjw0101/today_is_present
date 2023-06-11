package com.example.today_;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;



import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.accessibility.MyAccessibilityService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private DatabaseReference mDatabase;
    private TextView nameTextView;
    private EditText inputText;
    private EditText spell_check;
    private Button copyButton;
    private Button deleteTextButton;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 9001;
    private ImageView profileImageView;


    private static final int REQUEST_ACCESSIBILITY = 1;

    private final ActivityResultLauncher<Intent> accessibilitySettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(MainActivity.this, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();

                        }
                    });

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        accessibilitySettingsLauncher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        nameTextView = findViewById(R.id.nameTextView);
        SwitchCompat switchEnableService = findViewById(R.id.switchEnableService);
        Button logoutButton = findViewById(R.id.logoutButton);
        ImageButton imageButton5 = findViewById(R.id.imageButton5);
        ImageButton setting_ = findViewById(R.id.setting_);
        ImageButton image_store = findViewById(R.id.image_store);
        ImageButton image_book = findViewById(R.id.image_book);
        ImageButton help = findViewById(R.id.help);
        ImageButton image_search = findViewById(R.id.image_search);
        Button button = findViewById(R.id.button);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        inputText = findViewById(R.id.input_text);
        spell_check = findViewById(R.id.spell_check);
        copyButton = findViewById(R.id.copy);
        deleteTextButton = findViewById(R.id.delect_text);
        Button button1 = findViewById(R.id.button1);

        copyButton.setVisibility(View.GONE); // 버튼 숨기기
        deleteTextButton.setVisibility(View.GONE); // 버튼 숨기기

        // 로그인 상태인 경우 프로필 정보 표시
        updateProfileInfo();

        imageButton5.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, point.class);
            startActivity(intent);
        });

        image_store.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, store.class);
            startActivity(intent);
        });

        image_book.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, book.class);
            startActivity(intent);
        });

        image_search.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, searching.class);
            startActivity(intent);
        });

        help.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, help.class);
            startActivity(intent);
        });

        setting_.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        });

        button.setOnClickListener(v -> {
            // 접근성이 활성화되지 않은 경우 메시지를 표시하고 접근성 설정 화면을 엽니다.
            if (!isAccessibilityEnabled()) {
                Toast.makeText(MainActivity.this, "접근성 서비스를 활성화시켜주세요.", Toast.LENGTH_LONG).show();
                openAccessibilitySettings();
            }
        });

        copyButton.setOnClickListener(v -> {
            String textToCopy = spell_check.getText().toString();

            // ClipboardManager 객체 가져오기
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // 복사할 데이터 생성
            ClipData clipData = ClipData.newPlainText("text", textToCopy);

            // 클립보드에 데이터 설정
            clipboardManager.setPrimaryClip(clipData);

            // 복사되었다는 메시지를 사용자에게 보여줌
            Toast.makeText(MainActivity.this, "텍스트가 복사되었습니다.", Toast.LENGTH_SHORT).show();
        });

        deleteTextButton.setOnClickListener(v -> {
            inputText.setText("");
            spell_check.setText("");
            copyButton.setVisibility(View.GONE);
            deleteTextButton.setVisibility(View.GONE);
        });


        button1.setOnClickListener(v -> {
            copyButton.setVisibility(View.VISIBLE);
            deleteTextButton.setVisibility(View.VISIBLE);
            String input = inputText.getText().toString();
            sendRequestToFlask(input);
        });


        logoutButton.setOnClickListener(v -> signOut());

        switchEnableService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableAccessibilityService();
            } else {
                disableAccessibilityService();
            }
        });

        if (!isAccessibilityEnabled()) {
            enableAccessibilityService();
        }
    }

    private void sendRequestToFlask(String input) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url("http://192.168.0.102:5001/spell/?text=" + input)
                    .method("GET", null)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("error", "Connect Server Error: " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp1 = response.body().string();
                    JSONObject json;

                    try {
                        json = new JSONObject(resp1);
                        spell_check.setText(json.getString("after_word"));
                        Log.d("m", "Response Body is " + json.getString("before_word"));
                        Log.d("m", "Response Body is " + json.getString("after_word"));

                    } catch (JSONException e) {
                        throw new RuntimeException(e) ;
                    }
                }
            });
        } catch (Exception e) {
            Log.d("error", e.toString());
        }
    }


    private void updateProfileInfo() {
        // GoogleSignInAccount 객체 가져오기
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // 이름 가져오기
            String name = account.getDisplayName();
            // 프로필 사진 URL 가져오기
            Uri photoUri = account.getPhotoUrl();

            // 이름과 프로필 사진 표시
            nameTextView.setText(name);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_profile_image) // 기본 프로필 이미지 설정
                    .error(R.drawable.default_profile_image) // 에러 시에도 기본 프로필 이미지 사용
                    .diskCacheStrategy(DiskCacheStrategy.ALL); // 디스크 캐시 사용

            if (photoUri != null) {
                Glide.with(this)
                        .load(photoUri)
                        .apply(requestOptions)
                        .into(profileImageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.default_profile_image)
                        .apply(requestOptions)
                        .into(profileImageView);
            }
        } else {
            // 로그인 상태가 아닌 경우 기본 프로필 이미지 표시
            profileImageView.setImageResource(R.drawable.default_profile_image);
        }
    }

    @IgnoreExtraProperties
    public class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }

    }

    public void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }



    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
            profileImageView.setImageResource(R.drawable.default_profile_image);
            nameTextView.setText("");
            startActivity(new Intent(MainActivity.this, Login_activity.class));
            finish();
        });
    }

    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String serviceId = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                String[] accessibilityServices = settingValue.split(":");
                for (String accessibilityService : accessibilityServices) {
                    if (accessibilityService.equalsIgnoreCase(serviceId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private void enableAccessibilityService() {
        ComponentName componentName = new ComponentName(this, MyAccessibilityService.class);
        getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            CharSequence channelName = "Channel Name";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Toast.makeText(this, "접근성 서비스가 활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void disableAccessibilityService() {
        ComponentName componentName = new ComponentName(this, MyAccessibilityService.class);
        getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Toast.makeText(this, "접근성 서비스가 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACCESSIBILITY) {
            if (isAccessibilityEnabled()) {
                Toast.makeText(this, "접근성 서비스가 활성화되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "접근성 서비스가 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }





    public static MainActivity getInstance() {
        return instance;
    }
    public void setFloatingActionButtonText(String text) {
         //textView.setText(text);
    }
}
