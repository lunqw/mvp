**pvm**是一个自动生成mvp模式代码工具，提供以下功能：

1. 自动生成view接口；
2. presenter生命周期管理；
3. 工作线程/UI线程切换；

---
## 使用
工程build.gradle

	buildscript {
	    dependencies {
	        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
	    }
	}

模块build.gradle

	apply plugin: 'android-apt'
    compile 'com.github.lunqw.pvm:pvm:0.3.1'
    apt 'com.github.lunqw.pvm:pvm-compiler:0.3.1'

## 工作方式
	// 待完成
	//
	//

## Samples
### 一个View只有一个Presenter
	@PVM({LoginPresenter.class})
	public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
	    private LoginPresenter mLoginPresenter = new LoginPresenter();
	    private EditText mPassport;
	    private EditText mPassword;
	    private TextView mErrorText;
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_login);
	        PVManager.bind(this, mLoginPresenter, getWindow().getDecorView());
	        mPassport = (EditText) findViewById(R.id.et_passport);
	        mPassword = (EditText) findViewById(R.id.et_password);
	        mErrorText = (TextView) findViewById(R.id.tv_error_tips);
	    }
	
	    @Override
	    public void onClick(View v) {
	        final String passport = mPassport.getText().toString();
	        final String password = mPassword.getText().toString();
	        mLoginPresenter.login(passport, password);
	    }
	
	    @PVMSink
	    void onLoginSuccess(String token) {
	        final Intent intent = new Intent(this, MainActivity.class);
	        startActivity(intent);
	    }
	
	    @PVMSink
	    void onLoginFailed(int code, String message) {
	        final String error = String.format("%d: %s", code, message);
	        mErrorText.setText(error);
	    }
	}

	public class LoginPresenter implements Presenter {
	    private Executor mExecutor = Executors.newSingleThreadExecutor();
	    private LoginPresenterDelegate mDelegate;
	
	    @Override
	    public void onAttachedToView(Delegate delegate) {
	        mDelegate = (LoginPresenterDelegate) delegate;
	    }
	
	    @Override
	    public void onDetachedFromView(Delegate delegate) {
	        // TODO: release resources
	    }
	
	    public void login(final String passport, final String password) {
	        mExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	                if (System.currentTimeMillis() % 2 == 0) {
	                    mDelegate.onLoginSuccess(passport + "-" + password);
	                } else {
	                    mDelegate.onLoginFailed(1, "Unknown error");
	                }
	            }
	        });
	    }
	}

### 一个View含有多个Presenter

	@PVM({MainPresenter.class, UserPresenter.class, SettingPresenter.class})
	public class MainActivity extends AppCompatActivity {
	    private static final String TAG = "MainActivity";
	    private Presenter[] mPresenters = {new MainPresenter(), new UserPresenter(), new SettingPresenter()};
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        PVManager.bind(this, Arrays.asList(mPresenters), getWindow().getDecorView());
	    }
	
	    @PVMSink
	    void onGetProducts(List<Object> products) {
	        Log.d(TAG, "onGetProducts");
	    }
	
	    @PVMSink(1)
	    void onGetUserInfo(String nick, char sex, int age) {
	        Log.d(TAG, "onGetUserInfo");
	    }
	
	    @PVMSink(2)
	    void onGetUserSetting(Map<String, String> settings) {
	        Log.d(TAG, "onGetUserSetting");
	    }
	}
	
	public class MainPresenter implements Presenter {
	    private MainPresenterDelegate mDelete;
	
	    @Override
	    public void onAttachedToView(Delegate delegate) {
	        mDelete = (MainPresenterDelegate) delegate;
	        mDelete.onGetProducts(null);
	    }
	
	    @Override
	    public void onDetachedFromView(Delegate delegate) {
	
	    }
	}
	
	public class UserPresenter implements Presenter {
	    private UserPresenterDelegate mDelete;
	
	    @Override
	    public void onAttachedToView(Delegate delegate) {
	        mDelete = (UserPresenterDelegate) delegate;
	        mDelete.onGetUserInfo("lunqw", 'M', 18);
	    }
	
	    @Override
	    public void onDetachedFromView(Delegate delegate) {
	
	    }
	}
	
	public class SettingPresenter implements Presenter {
	    private SettingPresenterDelegate mDelete;
	
	    @Override
	    public void onAttachedToView(Delegate delegate) {
	        mDelete = (SettingPresenterDelegate) delegate;
	        mDelete.onGetUserSetting(new HashMap<String, String>());
	    }
	
	    @Override
	    public void onDetachedFromView(Delegate delegate) {
	
	    }
	}