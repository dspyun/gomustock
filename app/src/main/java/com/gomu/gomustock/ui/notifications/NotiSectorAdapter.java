package com.gomu.gomustock.ui.notifications;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gomu.gomustock.R;
import com.gomu.gomustock.ui.format.FormatSector;

import java.util.ArrayList;
import java.util.List;

public class NotiSectorAdapter extends BaseAdapter{

        private static final String TAG = "SingleAdapter";

        private List<FormatSector> items=new ArrayList<>();
        Context context;

        public NotiSectorAdapter(Context context, List<FormatSector> items){
            this.context = context;
            this.items=items;
        }

        @Override
        public int getCount() { //최초에 화면의 갯수를 설정함
            Log.d(TAG, "getCount: ");
            return items.size();
        }

        @Override
        public FormatSector getItem(int position) { //아이템이 클릭될 때 아이템의 데이터를 도출
            Log.d(TAG, "getItem: ");
            return items.get(position);
        }

        @Override
        public long getItemId(int position) { //필수 아님
            Log.d(TAG, "getItemId: ");
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.noti_list_sector, null);
            }

            FormatSector sectorinfo =  getItem(position);
            TextView name = convertView.findViewById(R.id.name);
            TextView changerate = convertView.findViewById(R.id.changerate);
            TextView changerate_period = convertView.findViewById(R.id.changerate_period);
            TextView foreign = convertView.findViewById(R.id.foreign);
            TextView agency = convertView.findViewById(R.id.agency);
            TextView ant = convertView.findViewById(R.id.ant);
            TextView top1 = convertView.findViewById(R.id.top1);
            TextView top2 = convertView.findViewById(R.id.top2);

            name.setText(sectorinfo.name);
            changerate.setText(sectorinfo.changerate);
            changerate_period.setText(sectorinfo.changerate_period);
            foreign.setText(sectorinfo.foreign);
            agency.setText(sectorinfo.agency);
            ant.setText(sectorinfo.ant);
            top1.setText(sectorinfo.top1);
            top2.setText(sectorinfo.top2);
            return convertView;
        }
}
