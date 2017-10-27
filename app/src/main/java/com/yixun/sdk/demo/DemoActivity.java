package com.yixun.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yixun.sdk.demo.utils.AssetsFileUtil;
import com.idealsee.sdk.util.ISARTipsUtil;

import java.io.File;
import java.io.IOException;

public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnRightIn = (Button) findViewById(R.id.btn_activity_in_right);
        btnRightIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SDKDemoActivity.class);
                intent.putExtra("from_main", true);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        Button btnBottomIn = (Button) findViewById(R.id.btn_activity_in_bottom);
        btnBottomIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SDKDemoActivity.class);
                intent.putExtra("from_main", true);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.up_out);
            }
        });

//        离线识别时开启
//        setDataFilePath();
    }

    private void setDataFilePath() {
        String[] assetsList = new String[10];
        final String resourcePath = getExternalFilesDir(null) + File.separator + "Resources";
        try {
            assetsList = getAssets().list("resource");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (assetsList == null || assetsList.length <= 0) {
            ISARTipsUtil.showShortToast(this, "No offline resources in the assets folder!");
            return;
        }

        for (int i = 0; i < assetsList.length; i++) {
            AssetsFileUtil.putAssetsFilesToSDCard(this, "resource/" + assetsList[i], resourcePath);
        }
    }

}
