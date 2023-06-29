package com.gomu.gomustock.ui.notifications;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentNotificationsBinding;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.ui.format.FormatSector;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    ImageView infochart11, infochart12, infochart21, infochart22;
    MyWeb myweb;
    TextView textView;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        myweb= new MyWeb();
        //kr_zumimage = binding.ivMyzum;
        infochart11 = root.findViewById(R.id.chart11);
        infochart12 = root.findViewById(R.id.chart12);
        infochart21 = binding.chart21;
        infochart22 = binding.chart22;
        textView = root.findViewById(R.id.info_board);
        //textView= binding.textNotifications;

        String str = "차트분석&평가, 예측알고리즘, 네이버 종목고르기\n";
        str += "속도가속도 기법, 종목분석&평가 \n";
        str += "시장의 돈쏠림을 알 수 있게 \n";
        str += "섹터별 기관/외국인 매수매도 차트? 네이버활용";
        textView.setText(str);
        //mythread();

        FormatSector mysector = new FormatSector();
        mysector.init();
        List<FormatSector> sectorlist = new ArrayList<>();
        sectorlist.add(mysector);

        ListView sector_list = (ListView) root.findViewById(R.id.sector_listview);
        NotiSectorAdapter adapter = new NotiSectorAdapter(getActivity(),sectorlist);
        sector_list.setAdapter(adapter);

        sector_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),mid[position],Toast.LENGTH_SHORT).show();
            }
        });


        String kr_imageUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/world/day/NQcv1_end_up.png?1687375767000";
        Glide.with(context).load(kr_imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(infochart12);
        infochart12.setScaleType(ImageView.ScaleType.FIT_XY);

        String imageUrl_01 = "https://ssl.pstatic.net/imgfinance/chart/mobile/world/day/.IXIC_end_up.png?1687335359000";
                Glide.with(context).load(imageUrl_01)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(infochart11);
        infochart11.setScaleType(ImageView.ScaleType.FIT_XY);

        infochart11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                String kr_imageUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/world/day/.IXIC_end_up.png?1687335359000";
                Glide.with(context).load(kr_imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(infochart11);
                infochart11.setScaleType(ImageView.ScaleType.FIT_XY);
                //Toast.makeText(context, "update imageview", Toast.LENGTH_SHORT).show();

                 */
                dl_SectroInfo();
            }
        });

        infochart12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kr_imageUrl = "https://ssl.pstatic.net/imgfinance/chart/mobile/world/day/NQcv1_end_up.png?1687375767000";
                Glide.with(context).load(kr_imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(infochart12);
                infochart12.setScaleType(ImageView.ScaleType.FIT_XY);
                //Toast.makeText(context, "update imageview", Toast.LENGTH_SHORT).show();
            }
        });

        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }
    public void dl_SectroInfo() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myweb.getSectorInfo();
            }
        }).start();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}