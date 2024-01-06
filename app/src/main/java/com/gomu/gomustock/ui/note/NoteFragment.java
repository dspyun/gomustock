package com.gomu.gomustock.ui.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.gomu.gomustock.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    Button editbutton, savebutton;
    EditText mynote;

    FileControl filecontrol;
    String mymemo;
    String MEMOFILE = "memo.txt";

    public NoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoteFragment newInstance(String param1, String param2) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_note, container, false);

        editbutton = view.findViewById(R.id.memoedit);
        savebutton = view.findViewById(R.id.memosave);
        mynote = view.findViewById(R.id.myeditor);
        filecontrol = new FileControl();

        mymemo = Read_Information(MEMOFILE);
        mynote.setText(mymemo);
        setUseableEditText(mynote, false);

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save_Information(MEMOFILE);
                setUseableEditText(mynote, false);
                hideBottomNavigation(false);
            }
        });
        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideBottomNavigation(true);
                setUseableEditText(mynote, true);
                mymemo = Read_Information(MEMOFILE);
                mynote.setText(mymemo);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
    public void hideBottomNavigation(Boolean bool) {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.nav_view);

        if (bool == true)
            bottomNavigation.setVisibility(View.GONE);
        else
            bottomNavigation.setVisibility(View.VISIBLE);
    }
    public String Read_Information(String filename) {
        String readfilename;
        readfilename = filename;//저장할 파일 이름설정
        //fname="/Label/"+"my-todo"+".txt";//저장할 파일 이름설정
        return filecontrol.ReadTextFile(readfilename);
    }

    public void Save_Information(String filename) {
        String savefilename;
        savefilename = filename;//저장할 파일 이름설정
        //Toast.makeText(getActivity().getApplicationContext(),"File save "+savefilename, Toast.LENGTH_SHORT).show();
        String content = mynote.getText().toString();
        filecontrol.WriteTextFile(savefilename, content);
    }

    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);

    }
}
