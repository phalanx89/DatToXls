package dtx.weaversmind.com.dattoxls;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelConverter extends SQLiteOpenHelper {
    private static final String TAG = ExcelConverter.class.getSimpleName();

    private Context mContext = null;
    private String mSavePath = "";

    public ExcelConverter(Context context, String strDbName, int version) {
        super(context, strDbName, null, version);

        mContext = context;
        mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/XlsFiles";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "onCreate db version is " + db.getVersion());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade db version is " + db.getVersion() + " oldVer:" + oldVersion + ", newVer:" + newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onDowngrade db version is " + db.getVersion() + " oldVer:" + oldVersion + ", newVer:" + newVersion);
        /*
        * 버전이 1로 지정되어 있기 때문에 oldVersion > newVersion인 상태가 발생하는데(기존 DB가 버전이 1미만인 경우는 없을테니까)
        * oldVersion > newVersion인 상태에서 super.onDowngrade()를 타버리면 죽기때문에 그냥 넘어간다. (DB를 읽기만 하기때문에 버전 건드릴 이유가 없다!)
        * */
    }

    private Cursor readByQuery(String strQuery) {
        SQLiteDatabase db = getReadableDatabase();
        return readByQuery(db, strQuery);
    }

    private Cursor readByQuery(SQLiteDatabase db, String strQuery) {
        if (db != null) {
            try {
                Cursor c = db.rawQuery(strQuery, new String[]{});
                if (c.moveToFirst()) {
                    return c;
                } else {
                    c.close();
                    return null;
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "readByQuery error: " + e.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //dat convert to xls
    public void makeXlsFiles() {
        String[] arrTables = null;

        String strQuery = "SELECT name FROM sqlite_master WHERE type = 'table'";
        Cursor c = readByQuery(strQuery);
        if (c != null) {
            arrTables = new String[c.getCount()];
            int idx = 0;
            do {
                arrTables[idx] = getCursorString(c, 0);
                Log.e(TAG, "Table Name[" + arrTables[idx] + "] is detected!");
                idx++;
            } while (c.moveToNext());
            c.close();
        }

        if (arrTables != null) {
            for (String strTableName : arrTables) {
                strQuery = "SELECT * FROM " + strTableName;
                c = readByQuery(strQuery);
                if (c != null) {
                    String[] arrColumns = new String[c.getColumnCount()];
                    for (int i = 0; i < arrColumns.length; i++) {
                        arrColumns[i] = c.getColumnName(i);
                    }
                    exportToExcel(c, strTableName, strTableName + ".xls", arrColumns);
                }
                if (c != null) {
                    if (!c.isClosed()) {
                        c.close();
                    }
                }
            }
        }
        Log.e(TAG, "엑셀데이터 생성 끝!");
        Toast.makeText(mContext, "엑셀데이터 생성 완료! XlsFiles 폴더를 확인해주세요!", Toast.LENGTH_SHORT).show();
    }

    //db convert to Excel
    private void exportToExcel(Cursor cursor, String strTableName, String strFileName, String[] arrColumns) {
        //Saving file in external storage
        /*File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/XlsFiles");*/
        File directory = new File(mSavePath);

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        //file path
        File file = new File(directory, strFileName);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet(strTableName.length() > 0 ? strTableName : "default", 0);

            try {
                for (int i = 0; i < arrColumns.length; i++) {
                    sheet.addCell(new Label(i, 0, arrColumns[i]));
                }
                if (cursor.moveToFirst()) {
                    do {
                        int curPos = cursor.getPosition() + 1;
                        for (int j = 0; j < arrColumns.length; j++) {
                            String strValue = getCursorString(cursor, j);

                            sheet.addCell(new Label(j, curPos, strValue));
                        }
                    } while (cursor.moveToNext());
                }
                //closing cursor
                cursor.close();

                Log.e(TAG, strFileName + " 생성 완료");
            } catch (RowsExceededException e) {
                e.printStackTrace();
                Log.e(TAG, strFileName + " 생성 실패");
            } catch (WriteException e) {
                e.printStackTrace();
                Log.e(TAG, strFileName + " 생성 실패");
            }
            workbook.write();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, strFileName + " 생성 실패");
        }
    }

    public static String getCursorString(Cursor c, int idx) {
        String ret = "";

        if (c != null && !c.isNull(idx)) {
            try {
                ret = c.getString(idx);
            } catch (SQLiteException e) {
                ret = new String(c.getBlob(idx));
            }
        }

        return ret;
    }

    public void setSavePath(String path) {
        mSavePath = path;
    }
}