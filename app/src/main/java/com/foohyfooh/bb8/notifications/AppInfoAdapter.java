package com.foohyfooh.bb8.notifications;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.foohyfooh.bb8.R;

import java.util.Collections;
import java.util.List;

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.AppInfoViewHolder> {

    private final AppCompatActivity context;
    private final PackageManager packageManager;
    private final List<ApplicationInfo> infos;
    private final ConfigDao dao;

    public AppInfoAdapter(AppCompatActivity context) {
        this.context = context;
        packageManager = context.getPackageManager();
        infos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(infos, (info1, info2) -> info1.loadLabel(packageManager).toString()
                .compareTo(info2.loadLabel(packageManager).toString()));
        dao = ConfigDatabase.getInstance(context).dao();
    }

    @Override
    public AppInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_application_info_item, parent, false);
        return new AppInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AppInfoViewHolder holder, int position) {
        final ApplicationInfo info = infos.get(position);
        holder.icon.setImageDrawable(info.loadIcon(packageManager));
        holder.icon.setContentDescription(String.format(context.getString(R.string.icon_description), info.loadLabel(packageManager)));
        holder.name.setText(info.loadLabel(packageManager));

        Config config = dao.get(info.packageName);
        if (config != null){
            holder.enable.setVisibility(Button.GONE);
            holder.configure.setVisibility(Button.VISIBLE);
        }else{
            holder.enable.setVisibility(Button.VISIBLE);
            holder.configure.setVisibility(Button.GONE);
            holder.enable.setOnClickListener(view -> {
                dao.insert(new Config(info.packageName));
                holder.enable.setVisibility(Button.GONE);
                holder.configure.setVisibility(Button.VISIBLE);
            });
        }

        holder.configure.setOnClickListener(view -> {
            Intent configureIntent = new Intent(context, ConfigureActivity.class);
            configureIntent.putExtra(ConfigureActivity.EXTRA_PACKAGE_NAME, info.packageName);
            context.startActivity(configureIntent);
        });
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class AppInfoViewHolder extends RecyclerView.ViewHolder{

        private final ImageView icon;
        private final TextView name;
        private final Button enable, configure;
        public AppInfoViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.appInfo_icon);
            name = view.findViewById(R.id.appInfo_name);
            enable = view.findViewById(R.id.appInfo_enable);
            configure = view.findViewById(R.id.appInfo_configure);
        }
    }
}
