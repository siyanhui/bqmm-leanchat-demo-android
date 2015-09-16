package com.avoscloud.leanchatlib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * Created by wli on 15/7/24.
 */
public class AVBaseActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public void showToast(String content) {
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
  }

  public void showToast(int res) {
    Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
  }


  //TODO
  public void startIntent(Intent intent) {}

  public void startAction(String action) {}

  public void startActivity(Class<?> cls, int requestCode) {
    startActivityForResult(new Intent(this, cls), requestCode);
  }
}
