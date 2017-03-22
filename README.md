**pvm**是一个自动生成mvp模式代码工具，提供以下功能：

1. 自动生成view接口；
2. presenter生命周期管理；
3. 工作线程/UI线程切换；

---
### 使用
工程build.gradle

	buildscript {
	    dependencies {
	        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
	    }
	}

模块build.gradle

	apply plugin: 'android-apt'
    compile 'com.github.lunqw.pvm:pvm:0.1.3'
    apt 'com.github.lunqw.pvm:pvm-compiler:0.1.3'

### 工作方式
	// 待完成
	//
	//

### Sample
View

	@PVM(presenter = LoginPresenter.class)
	public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
	    LoginPresenter mPresenter;
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_login);
	        mPresenter = (LoginPresenter) PVManager.INSTANCE.bind(this,
	                getWindow().getDecorView());
	    }
	
	    @Override
	    public void onClick(View v) {
	        mPresenter.login(passport, password);
	    }
	
	    @PVMSink
	    void onLoginSuccess(String token) {
	        // TODO: login success
	    }
	
	    @PVMSink
	    void onLoginFailed(int code, String message) {
			// TODO: login failed
	    }
	}

Presenter

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