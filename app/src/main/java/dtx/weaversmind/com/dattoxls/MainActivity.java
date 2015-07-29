package dtx.weaversmind.com.dattoxls;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    private int mKillCount = 0;

    private Button mBtnConvert = null;
    private TextView mTvFilePath = null;
    private LinearLayout mLlyMain = null;
    private FileListView mFileListView = null;

    private ExcelConverter mExcelConverter = null;

    private String mSelPath = "/sdcard/";
    private String mFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnConvert = (Button) findViewById(R.id.btnConvert);
        mTvFilePath = (TextView) findViewById(R.id.tvFilePath);
        mLlyMain = (LinearLayout) findViewById(R.id.llyMain);

        mFileListView = new FileListView(this);
        mFileListView.setPath(mSelPath);
        mFileListView.setFocusable(true);
        mFileListView.setFocusableInTouchMode(true);
        mLlyMain.addView(mFileListView);

        /* Listener setting */
        mFileListView.setOnPathChangedListener(new OnPathChangedListener() {
            @Override
            public void onChanged(String path) {
                // TODO Auto-generated method stub
                mSelPath = path;
                mTvFilePath.setText("경로: " + path);
                mBtnConvert.setEnabled(false);
                mKillCount = 0;
            }
        });

        mFileListView.setOnFileSelected(new OnFileSelectedListener() {
            @Override
            public void onSelected(String path, String fileName) {
                // TODO Auto-generated method stub
                mSelPath = path;
                mFileName = fileName;
                mTvFilePath.setText("경로: " + mSelPath + mFileName);
                mBtnConvert.setEnabled(true);
                mKillCount = 0;
            }
        });

        mBtnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strExtension = mFileName.substring(mFileName.length() - 4, mFileName.length());
                if (strExtension.equalsIgnoreCase(".dat")) {
                    mExcelConverter = new ExcelConverter(getApplicationContext(), mSelPath + mFileName, 1);
                    mExcelConverter.makeXlsFiles();
                } else {
                    Toast.makeText(getApplicationContext(), "DB파일이 아닙니다! 다른파일을 선택해주세요!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void _finish() {
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 뒤로가기 키를 눌렀을때
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(mKillCount ==0){
                mKillCount++;
                Toast.makeText(this, "종료하시려면 한번더 누르세요", Toast.LENGTH_SHORT).show();
            } else
                _finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
