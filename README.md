**pvm**是一个自动生成mvp模式代码工具，提供以下功能：

1. 自动生成view接口；
2. presenter生命周期管理；
3. 工作线程/UI线程切换；

---
### 使用
#####工程build.gradle
	buildscript {
	    dependencies {
	        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
	    }
	}

#####模块build.gradle
	apply plugin: 'android-apt'
    compile 'com.github.lunqw.pvm:pvm:0.1.2'
    apt 'com.github.lunqw.pvm:pvm-compiler:0.1.2'

### 工作方式
	// 待完成
	//
	//

### Sample
#####View
	@PVM(presenter = LoginPresenter.class)
	public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
	    @PVMSink
	    void onLoginSuccess(String token) {
	        Intent intent = new Intent(this, MainActivity.class);
	        startActivity(intent);
	    }
	
	    @PVMSink
	    void onLoginFailed(int code, String message) {
	        final String error = String.format("%d: %s", code, message);
	        mErrorText.setText(error);
	    }
	}

#####Presenter
	public class LoginPresenter extends Presenter {
	    private LoginPresenterProxy mProxy;
	
	    @Override
	    public void onAttachedToView(Proxy proxy) {
	        mProxy = (LoginPresenterProxy) proxy;
	    }
	
	    public void login(final String passport, final String password) {
	        mProxy.onLoginSuccess(passport + "-" + password);
	    }
	}