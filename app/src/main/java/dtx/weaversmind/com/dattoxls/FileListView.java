package dtx.weaversmind.com.dattoxls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileListView extends ListView {

    public FileListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    public FileListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public FileListView(Context context) {
        super(context);

        init(context);
    }

    private void init(Context context) {
        _Context = context;
        //setOnItemClickListener(_OnItemClick);
    }

    private Context _Context = null;
    private ArrayList<FileInfo> _List = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> _FolderList = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> _FileList = new ArrayList<FileInfo>();
    private CustomAdapter _Adapter = null;

    // Property
    private String _Path = "";

    //
    static File[] files;
    static File file;

    // Event
    private OnPathChangedListener _OnPathChangedListener = null;
    private OnFileSelectedListener _OnFileSelectedListener = null;

    private boolean openPath(String path) {
        _FolderList.clear();
        _FileList.clear();

        file = new File(path);
        files = file.listFiles();
        if (files == null) return false;

        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                //_FolderList.add("<" + files[i].getName() + ">");
                _FolderList.add(new FileInfo("<" + files[i].getName() + ">", files[i].length(), files[i].lastModified(), true));
            } else {
                _FileList.add(new FileInfo(files[i].getName(), files[i].length(), files[i].lastModified(), false));
            }
        }

        //Collections.sort(_FolderList);
        //Collections.sort(_FileList);

        if(! path.equals("/")) {
            //_FolderList.add(0, "<뒤로가기>");
            _FolderList.add(0, new FileInfo("<뒤로가기>", 0, 0, true));
        }

        return true;
    }

    private void updateAdapter() {
        _List.clear();
        _List.addAll(_FolderList);
        _List.addAll(_FileList);

        //_Adapter = new ArrayAdapter<String>(_Context, android.R.layout.simple_list_item_1, _List);
        _Adapter = new CustomAdapter(_List);
        setAdapter(_Adapter);
    }

    public void setPath(String value) {

        if (value.length() == 0) {
            value = "/";
        } else {
            String lastChar = value.substring(value.length()-1, value.length());
            if (lastChar.matches("/") == false) value = value + "/";
        }

        if (openPath(value)) {
            _Path = value;
            updateAdapter();
            if (_OnPathChangedListener != null) _OnPathChangedListener.onChanged(value);
        }
    }

    public String getPath() {
        return _Path;
    }

    public void setOnPathChangedListener(OnPathChangedListener value) {
        _OnPathChangedListener = value;
    }

    public OnPathChangedListener getOnPathChangedListener() {
        return _OnPathChangedListener;
    }

    public void setOnFileSelected(OnFileSelectedListener value) {
        _OnFileSelectedListener = value;
    }

    public OnFileSelectedListener getOnFileSelected() {
        return _OnFileSelectedListener;
    }

    public String deleteRight(String value, String border) {
        String list[] = value.split(border);

        String result = "";

        for (int i=0; i<list.length; i++) {
            result = result + list[i] + border;
        }

        return result;
    }

    private String deleteLastFolder(String value) {
        String list[] = value.split("/");

        String result = "";

        for (int i=0; i<list.length-1; i++) {
            result = result + list[i] + "/";
        }

        return result;
    }

    /**
     * int position을 따로 추가하였습니다
     * 그 이유는 폴더 이름이 뒤로가기일경우 뒤로가기 처리가 되기 때문에 맨위에 있는 뒤로가기(position은 0)일때만
     * 상단 폴더로 넘어가도록 처리하였습니다
     */
    private String getRealPathName(String newPath, int position) {
        String path = newPath.substring(1, newPath.length()-1);
        if (path.matches("뒤로가기") && position == 0) {
            return deleteLastFolder(_Path);
        } else {
            return _Path + path + "/";
        }
    }

    /*private AdapterView.OnItemClickListener _OnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long id) {
            String fileName = getItemAtPosition(position).toString();
            if (fileName.matches("<.*>")) {
                setPath(getRealPathName(fileName, position));
            } else {
                if (_OnFileSelectedListener != null) _OnFileSelectedListener.onSelected(_Path, fileName);
            }
        }
    };*/

    public class CustomAdapter extends BaseAdapter {
        private ArrayList<FileInfo> mItemList = null;

        public CustomAdapter(ArrayList<FileInfo> itemList) {
            mItemList = itemList;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            CustomHolder holder = null;
            CheckBox cbSelect = null;
            ImageView ivType = null;
            TextView tvFileName = null;
            TextView tvFileDetail = null;

            // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
            if (convertView == null) {
                // view가 null일 경우 커스텀 레이아웃을 얻어 옴
                LayoutInflater inflater = (LayoutInflater) _Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_file_list, parent, false);

                cbSelect = (CheckBox) convertView.findViewById(R.id.cbSelect);
                ivType = (ImageView) convertView.findViewById(R.id.ivType);
                tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);
                tvFileDetail = (TextView) convertView.findViewById(R.id.tvFileDetail);

                // 홀더 생성 및 Tag로 등록
                holder = new CustomHolder();
                holder.cbSelect = cbSelect;
                holder.ivType = ivType;
                holder.tvFileName = tvFileName;
                holder.tvFileDetail = tvFileDetail;
                convertView.setTag(holder);
            } else {
                holder = (CustomHolder) convertView.getTag();
                cbSelect = holder.cbSelect;
                ivType = holder.ivType;
                tvFileName = holder.tvFileName;
                tvFileDetail = holder.tvFileDetail;
            }

            tvFileName.setText(mItemList.get(pos).fileName);
            tvFileDetail.setText(String.valueOf(mItemList.get(pos).length));

            // 리스트 아이템을 터치 했을 때 이벤트 발생
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 터치 시 해당 아이템 이름 출력
                    Toast.makeText(_Context, "리스트 클릭 : " + mItemList.get(pos).fileName, Toast.LENGTH_SHORT).show();
                    String fileName = mItemList.get(pos).fileName;//getItemAtPosition(pos).toString();
                    if (fileName.matches("<.*>")) {
                        setPath(getRealPathName(fileName, pos));
                    } else {
                        if (_OnFileSelectedListener != null) {
                            _OnFileSelectedListener.onSelected(_Path, fileName);
                        }
                    }
                }
            });

            // 리스트 아이템을 길게 터치 했을 떄 이벤트 발생
            convertView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 터치 시 해당 아이템 이름 출력
                    Toast.makeText(_Context, "리스트 롱 클릭 : "+mItemList.get(pos).fileName, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            return convertView;
        }

        // 외부에서 아이템 추가 요청 시 사용
        public void add(FileInfo item) {
            mItemList.add(item);
        }

        // 외부에서 아이템 삭제 요청 시 사용
        public void remove(int position) {
            mItemList.remove(position);
        }
    }

    public class CustomHolder {
        CheckBox cbSelect;
        ImageView ivType;
        TextView tvFileName;
        TextView tvFileDetail;
    }

    public class FileInfo {
        String fileName = null;
        long length = 0;
        long lastModified = 0;
        boolean isDirectory = false;

        public FileInfo(String fileName, long length, long lastModified, boolean isDirectory) {
            this.fileName = fileName;
            this.length = length;
            this.lastModified = lastModified;
            this.isDirectory = isDirectory;
        }
    }
}
