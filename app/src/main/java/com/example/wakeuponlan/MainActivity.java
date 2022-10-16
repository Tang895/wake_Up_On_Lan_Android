package com.example.wakeuponlan;

import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public void showMes(String mes){
        Toast t=Toast.makeText(MainActivity.this,mes,Toast.LENGTH_LONG);
        t.show();
    }
    public void saveCacheFile(String mac,String ip,File catchFile){
        if(getCheckStatus()) {
            String save = mac + "&&" + ip;
            try {
            FileOutputStream fos = new FileOutputStream(catchFile);
            fos.write(save.getBytes(StandardCharsets.UTF_8));
             }catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            return ;
        }
    }
    public void work(String mac,String ip){
        //String del=mac+ip;

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {

                //6对“FF”前缀+16次重复MAC地址
                //6+16*6=
                byte[] mes=new byte[128];
                for(int i=0;i<6;i++){
                    mes[i]=(byte)0xff;
                }
                //6 bytes, 6 bytes 16*6+6

                String[] hex=mac.split("-");
                //多传输两次应该没啥问题。。。。
                int ite=6;
                for(int i=0;i<18;i++){
                    for(int j=0;j<hex.length;j++){
                        mes[ite]=(byte)Integer.parseInt(hex[j],16);
                        ite++;
                    }
                }



                //System.out.println(Arrays.toString(hex));
                try {
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(mes, mes.length, InetAddress.getByName(ip), 9);


                    ds.send(dp);
                    ds.close();

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public boolean getCheckStatus(){
        CheckBox cb=(CheckBox) findViewById(R.id.checkBox);
        return cb.isChecked();
    }
    public void createChtchFile() throws Exception{
        File.createTempFile("MAC_IP.txt",null,this.getCacheDir());
    }
    public void setMacipText(String sb){

        String[] sbb=sb.split("&&");
        System.out.println("文件内容："+sb);
        String ip=sbb[1];
        String mac=sbb[0];
        System.out.println("getIP:"+sb+ip+mac);
        EditText et_IP=(EditText) findViewById(R.id.editTextforIP);
        EditText et_Mac=(EditText) findViewById(R.id.editTextforMAC);
        et_IP.setText(ip);
        et_Mac.setText(mac);
    }
    public boolean judegUserInput(String mac,String ip){
        //mac:11-22-33-44-55-66
        //ip:192.168.23.xxx
        String mac_match="([0-9a-fA-F]{2}-){5}[0-9a-fA-F]{2}";
        String ip_match= "\\d{1,3}\\.\\d{1,3}.\\d{1,3}\\.\\d{1,3}";
        System.out.println(ip+mac);
        boolean mac_isRight= Pattern.matches(mac_match,mac);

        boolean ip_isRight=Pattern.matches(ip_match,ip);
        if(mac_isRight && ip_isRight && mac.length()=="11-22-33-44-55-66".length()){
            return true;
        }else{
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //访问文件

        File cacheFile = new File(this.getCacheDir(), "MAC_IP.txt");
        if(!cacheFile.exists()){
            try {
                createChtchFile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            try {
                FileInputStream fis = new FileInputStream(cacheFile);
                StringBuilder sb=new StringBuilder();
                while(fis.available()>0){
                    sb.append((char)fis.read());
                }
                //System.out.println("读取到的文件："+sb);
                setMacipText(sb.toString());

            }catch(Exception e){
                e.printStackTrace();
            }
        }


        Button bt = (Button) findViewById(R.id.button);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView ip_Text_View=(TextView) findViewById(R.id.editTextforIP);
                TextView mac_Text_View=(TextView) findViewById(R.id.editTextforMAC);
                String ip=ip_Text_View.getText().toString();
                String mac=mac_Text_View.getText().toString();

                //判断用户的输入
                if(!judegUserInput(mac,ip)){
                    showMes("你的ip输入或者mac输入有问题！");
                    return;
                }


                saveCacheFile(mac,ip,cacheFile);

                //System.out.println(ip+mac);
                //System.out.println("helo");

                showMes("正在发送数据。。。");
                work(mac,ip);
                showMes("已经发送数据。。。");
            }
        });

    }
}