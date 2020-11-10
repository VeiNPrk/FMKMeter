package com.example.fmkmeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.databinding.DataBindingUtil;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.fmkmeter.databinding.ActivityMainOldBinding;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_old extends AppCompatActivity implements ReaderAsyncTask.ReaderAsyncTaskListener,
        CalculateAsyncTask.CalculateAsyncTaskListener {

    public static final String TAG = "MainActivity_old";
    static Context DeviceInformationContext;
    D2xxManager ftdid2xx;
	FT_Device ftDev = null;
    public int iavailable = 0;
    static Context DeviceUARTContext;
	public static final int DELAY_READ_THREAD = 100;
	public static final int readLength = 65536;
    int devCount = 0;
    boolean isSingle=false;
	int openIndex = 0;
	int currentIndex = -1;
	byte[] readData;
    List<Integer> outData;
    //List<Entry> entries = null;
    char[] readDataToText;
	byte[] buff;
    String strReadData="";
    int ftStatus=0;
    //public readThread read_thread;
	public Thread readThread = null;
    public boolean bReadThreadGoing = false;
    boolean uart_configured = false;
    static int iEnableReadFlag = 1;
	LineData chartData = null;
	ReaderAsyncTask readerAsyncTask;
    List<String> spinnerListDevices = new ArrayList<String>();
    D2xxManager.FtDeviceInfoListNode[] deviceList = null;
    ActivityMainOldBinding binding;
	//List<String> infoDevice = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_old);
        initViews();
		readThread = new Thread(runnable);
		readThread.setDaemon(true);
        DeviceUARTContext = getApplicationContext();
        readData = new byte[readLength];
        readDataToText= new char[readLength];
        outData = new ArrayList<Integer>();
        //entries = new ArrayList<Entry>();
        InitDeviceList();
        setSpinners();
    }

    private void initViews(){
        try {
            ftdid2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            ex.printStackTrace();
        }
        /*numberDeviceValue = findViewById(R.id.numDev);
        tvCount = findViewById(R.id.tvCount);
        //error_Information = findViewById(R.id.errorInformation);
        spnSi = findViewById(R.id.spnSi);
        spnDevices = findViewById(R.id.spnDevices);
		chart = findViewById(R.id.chart);
		tvRead = findViewById(R.id.tvRead);*/
        binding.tvRead.setMovementMethod(new ScrollingMovementMethod());
        //binding.btnRefreshDevice = findViewById(R.id.btnInfo);
        binding.btnInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                if(deviceList!=null && deviceList.length>0){
                    InfoDialogFragment dialog = InfoDialogFragment.newInstance(deviceList[openIndex]);
                    dialog.show(getSupportFragmentManager(), InfoDialogFragment.TAG);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Нет подключенных устройств", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //binding.btnRead = findViewById(R.id.btnRefresh);
        binding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                InitDeviceList();//EnableRead();
                /*RefrestDeviceInformation(v);*/
            }
        });

        //binding.btnResult = findViewById(R.id.btnResult);
        binding.btnResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Log.d("RESULT","RESULT "+outData.size());
                startCalcTask();
				//tvRead.setText(strReadData);
                //initChart();
                //EnableRead();
                /*RefrestDeviceInformation(v);*/
            }
        });
		
		//btnWrite = findViewById(R.id.btnWrite);

        binding.btnWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                SetSettings();
                /*RefrestDeviceInformation(v);*/
            }
        });
/*
        btnStart = findViewById(R.id.btnStartIzm);
        btnFinish = findViewById(R.id.btnFinishIzm);
        btnSingle = findViewById(R.id.btnSinglIzm);
*/
        binding.btnStartIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartIzmNew();
            }
        });
        binding.btnFinishIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinishIzmNew();
            }
        });
        binding.btnSingleIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleIzmNew();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getApplicationContext().registerReceiver(mUsbPlugEvents, filter);

    }

    private void startCalcTask(){
        CalculateAsyncTask calcTask = new CalculateAsyncTask(this, 2000);
        //calcTask.setListener(this);
        calcTask.execute();
    }

    private void SetSettings() {
        buff = new byte[3];
        buff[0] = (byte) 0xA5;
        buff[1] = 4;
        buff[2] = (byte)240;
        int i = ftDev.getQueueStatus();
        ftDev.write(buff, buff.length);
        binding.tvRead.setText("Настройки установлены");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setSpinners() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_item, spinnerListDevices);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        binding.spnDevices.setAdapter(adapter);
        binding.spnDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("onItemSelected", view.toString());
                try {
                    ConnectDevice(position);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*UserClass user = (UserClass)spnIdUser.getSelectedItem();
                Log.d("onItemSelected", ""+user.getId());
                int selector = selectorsArray[spnSelector.getSelectedItemPosition()];
                //int userid = Integer.parseInt(parent.getItemAtPosition(position).toString());
                onChangeSelector(user.getId(), selector);*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v("onNothingSelected", parent.toString());
            }
        });

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.array_si, android.R.layout.simple_spinner_dropdown_item);
        binding.spnSi.setAdapter(adapter1);
        binding.spnSi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("onItemSelected", view.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void InitDeviceList() {
        int devCount = 0;
        try {
            String deviceName = "";
            devCount = ftdid2xx.createDeviceInfoList(getApplicationContext());
            Log.i("FtdiModeControl","Device number = " + Integer.toString(devCount));
            if (devCount > 0) {
                binding.tvDevCount.setVisibility(View.GONE);
                deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
                ftdid2xx.getDeviceInfoList(devCount, deviceList);
                for(D2xxManager.FtDeviceInfoListNode device : deviceList){
                    switch (device.type) {
                        case D2xxManager.FT_DEVICE_232B:
                            deviceName="FT232B";
                            break;

                        case D2xxManager.FT_DEVICE_8U232AM:
                            deviceName="FT8U232AM";
                            break;

                        case D2xxManager.FT_DEVICE_UNKNOWN:
                            deviceName="Unknown device";
                            break;

                        case D2xxManager.FT_DEVICE_2232:
                            deviceName="FT2232";
                            break;

                        case D2xxManager.FT_DEVICE_232R:
                            deviceName="FT232R";
                            break;

                        case D2xxManager.FT_DEVICE_2232H:
                            deviceName="FT2232H";
                            break;

                        case D2xxManager.FT_DEVICE_4232H:
                            deviceName="FT4232H";
                            break;

                        case D2xxManager.FT_DEVICE_232H:
                            deviceName="FT232H";
                            break;
                        case D2xxManager.FT_DEVICE_X_SERIES:
                            deviceName="FTDI X_SERIES";
                            break;
                        default:
                            deviceName="FT232B";
                            break;
                    }
                    spinnerListDevices.add(deviceName);

                }
            } else {
                binding.tvDevCount.setVisibility(View.VISIBLE);
                binding.tvDevCount.setText("Number of devices: 0");
                //deviceName.setText("Device Name : No device");
            }
        }catch (Exception ex)
        {
            binding.tvRead.setText(ex.toString());
        }
    }

    public void ConnectDevice(int deviceIndex) throws InterruptedException {
        int devCount = 0;
        openIndex = deviceIndex;
        try {
            devCount = ftdid2xx.createDeviceInfoList(getApplicationContext());
            Log.i("FtdiModeControl","Device number = " + Integer.toString(devCount));
            if (devCount > 0) {
                binding.tvDevCount.setVisibility(View.GONE);
                connectFunction();
            } else {
                binding.tvDevCount.setVisibility(View.VISIBLE);
                binding.tvDevCount.setText("Number of devices: 0");
            }
        }catch (Exception ex)
        {
            binding.tvRead.setText(ex.toString());
        }
    }

    /*public void RefrestDeviceInformation(View view) {
        try {
            GetDeviceInformation();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            String s = e.getMessage();
            if (s != null) {
                error_Information.setText(s);
            }
            e.printStackTrace();
        }
    }*/

    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    InitDeviceList();
                    //ConnectDevice(openIndex);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        binding.tvRead.setText(s);
                        //error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }
        }
    };
	
	public void connectFunction()
	{
		int tmpProtNumber = openIndex + 1;

		if( currentIndex != openIndex )
		{
			if(null == ftDev)
			{
				ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
			}
			else
			{
				synchronized(ftDev)
				{
					ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);
				}
			}
			uart_configured = false;
		}
		else
		{
			Toast.makeText(DeviceUARTContext,"Device port " + tmpProtNumber + " is already opened",Toast.LENGTH_LONG).show();
			return;
		}

		if(ftDev == null)
		{
			Toast.makeText(DeviceUARTContext,"open device port("+tmpProtNumber+") NG, ftDev == null", Toast.LENGTH_LONG).show();
			return;
		}
			
		if (true == ftDev.isOpen())
		{
			currentIndex = openIndex;
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") OK", Toast.LENGTH_SHORT).show();
			//StartThread();
			/*if(false == bReadThreadGoing){
			    read_thread = new readThread(handler);
				read_thread.start();
				bReadThreadGoing = true;
			}*/
		}
		else 
		{			
			Toast.makeText(DeviceUARTContext, "open device port(" + tmpProtNumber + ") NG", Toast.LENGTH_LONG).show();
			//Toast.makeText(DeviceUARTContext, "Need to get permission!", Toast.LENGTH_SHORT).show();			
		}
	}
	
	private void StartThread() {
		if(false == bReadThreadGoing){
		    //на случай не запуска просто потока//
            readThread = new Thread(runnable);
            bReadThreadGoing = true;
			readThread.start();
		}
	}

    private void StartThreadNew(/*boolean bReadThreadGoing,*/FT_Device dev, boolean isSingle) {
        readerAsyncTask = new ReaderAsyncTask(dev, isSingle);
        readerAsyncTask.setListener(this);
        readerAsyncTask.execute();
    }
	
	private void StopThread() {
		if (readThread != null) {
			bReadThreadGoing = false;
			readThread.interrupt();
            /*Thread dummy = readThread;
            readThread = null;
            dummy.interrupt();*/
        }
	}

    private void StopThreadNew() {
        if (readerAsyncTask == null) return;
        Log.d(TAG, "cancel result: " + readerAsyncTask.cancel(false));
    }
	
	public void EnableRead (){    	
    	iEnableReadFlag = (iEnableReadFlag + 1)%2;
    	    	
		if(iEnableReadFlag == 1) {
			ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();
			//readEnButton.setText("Read Enabled");
		}
		else{
			ftDev.stopInTask();
			//readEnButton.setText("Read Disabled");
		}
    }

    public void SendMessage() {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ftDev.setLatencyTimer((byte) 16);
//		ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
		/*buff=new byte[readLength];
		
		buff[0] = 0xA5; buff[1] = 4;*/
		buff=new byte[readLength];
		buff[0] = (byte)0xA5; buff[1] = 0x03;
		//String writeData = writeText.getText().toString();
		//byte[] OutData = writeData.getBytes();
		ftDev.write(buff, buff.length);
		
		//ftDev.write(OutData, writeData.length());
    }
    public void SingleIzm() {
        Log.d("izm", "SingleIzm");
        strReadData="";
        outData.clear();

        iavailable = ftDev.getQueueStatus();
        /*ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
        ftDev.purge((byte) (D2xxManager.FT_PURGE_RX));*/
        if(iavailable>0)
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        if(ftDev.isOpen())
            ftDev.close();
        ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.restartInTask();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       /* do {
            ftStatus = ftDev.restartInTask();
        } while (ftStatus != D2xxManager.FT_OK);*/
            //ftDev.read(readData, iavailable);
        //ftDev.resetDevice();
        isSingle=true;
        //read_thread.start();
	    try {
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x03;
            int i = ftDev.getQueueStatus();
            int i1 = ftDev.write(buff, buff.length);
            //ftDev.restartInTask();
            i = ftDev.getQueueStatus();
            binding.tvRead.setText("SingleIzm");
            i = ftDev.getQueueStatus();
            StartThread();
        }
        catch (Exception ex)
        {
            binding.tvRead.setText(ex.toString());
            //Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
        }

    }
    public void StartIzm() {
	    Log.d("izm", "StartIzm");
        StopThread();
        strReadData="";
        outData.clear();
        iavailable = ftDev.getQueueStatus();

        //ftDev.purge((byte) (D2xxManager.FT_PURGE_TX));
        //ftDev.purge((byte) (D2xxManager.FT_PURGE_RX));
        if(iavailable>0)
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        if(ftDev.isOpen())
            ftDev.close();
        ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.restartInTask();
        ftDev.resetDevice();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.getQueueStatus();
            
	    try {
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x06;
            int i = ftDev.getQueueStatus();
            ftDev.write(buff, buff.length);
            //ftDev.restartInTask();
            binding.tvRead.setText("StartIzm");
            StartThread();
        }
        catch (Exception ex) {
            binding.tvRead.setText(ex.toString());
        }
    }

    public void FinishIzm(){
        Log.d("izm", "FinishIzm");
	    try {
            //bReadThreadGoing=false;
			StopThread();
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x02;
            ftDev.write(buff, buff.length);
            //ftDev.restartInTask();
            //ftDev.stopInTask();

            binding.tvRead.setText("FinishIzm");
            //iavailable = ftDev.getQueueStatus();
            /*iavailable = ftDev.getQueueStatus();
            if(iavailable>0)
                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));*/
            //strReadData="";
            //Toast.makeText(this, "FinishIzm", Toast.LENGTH_LONG);
        }
        catch (Exception ex) {
            binding.tvRead.setText(ex.toString());
            //Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
        }
    }

	final Handler handler =  new Handler()
    {
    	@Override
    	public void handleMessage(Message msg)
    	{
    	    binding.tvCountByteAll.setText("c="+outData.size());
    		if(iavailable > 0)
    		{
                binding.tvCountByteAll.setText("c="+outData.size());
    		}
    	}
    };

	Runnable runnable = new Runnable() {
        public void run() {
            int i;
            Log.d("tread", "bReadThreadGoing="+bReadThreadGoing);
            try {
				while(!Thread.currentThread().isInterrupted() &&/*если что закоментить часть слева*/ true == bReadThreadGoing)
				{
					Thread.sleep(DELAY_READ_THREAD);
					if(ftDev.stoppedInTask())
					{
						Log.d("ftDev", "stoped");
					}
					else
						Log.d("ftDev", "notstoped");

					synchronized(ftDev)
					{
						iavailable = ftDev.getQueueStatus();
						Log.d("iavailable", "iavailable="+iavailable);

						if (iavailable > 0) {
							strReadData="";
							if(iavailable > readLength){
								iavailable = readLength;
							}
							 
							ftDev.read(readData, iavailable);
							for (i = 0; i < iavailable-1; i=i+2) {
								int bt = readData[i] * 256 + readData[i + 1];
								outData.add(bt);
								strReadData+=bt+"*--*";
							}
							Message msg = handler.obtainMessage();
							handler.sendMessage(msg);
							if(isSingle){
							    isSingle=false;
                                StopThread();
						    }
						}
					}
				}
			} catch (InterruptedException e) {
				Log.d("tread", "Thread Interupt");
			}
			Log.d("tread", "bReadThreadGoing="+bReadThreadGoing+" поток завершён");
        }
    };
	
	private void initChartData(List<Entry> entries){
/*		entries.clear();
		int i = 1;
		if(outData.size()>0)
		for (int val : outData) {
			entries.add(new Entry(i, val));
			i++;
		}
		else
			entries.add(new Entry(1,0));
*/
		LineDataSet dataSet = new LineDataSet(entries, "Сигнал"); // add entries to dataset
		//dataSet.setColor(colorLine);
		//dataSet.setMode(LineDataSet.Mode.STEPPED);
        //dataSet.setLineWidth(3f);
		chartData = new LineData(dataSet);
        binding.chart.setData(chartData);
        binding.chart.invalidate();
        binding.chart.moveViewToX(0f);
        binding.chart.animateY(500);
	}
	
	
	private void initChart(List<Entry> entries){
        //chart.setOnChartGestureListener(this);
        //chart.setOnChartValueSelectedListener(this);
        binding.chart.setDrawGridBackground(false);
        binding.chart.getDescription().setEnabled(false);
        binding.chart.setTouchEnabled(true);
        binding.chart.setDragEnabled(true);
        binding.chart.setScaleEnabled(true);
        binding.chart.setPinchZoom(true);
		initChartData(entries);
        binding.chart.animateY(500);
    }

    public void SingleIzmNew() {
        Log.d(TAG, "SingleIzm");
        clearChart();
        iavailable = ftDev.getQueueStatus();
        if(iavailable>0)
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        if(ftDev.isOpen())
            ftDev.close();
        ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.restartInTask();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SetSettings();

        isSingle=true;
        //read_thread.start();
        try {
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x03;
            int i = ftDev.getQueueStatus();
            int i1 = ftDev.write(buff, buff.length);
            //ftDev.restartInTask();
            i = ftDev.getQueueStatus();
            binding.tvRead.setText("SingleIzm");
            i = ftDev.getQueueStatus();

            StartThreadNew(ftDev, true);
        }
        catch (Exception ex)
        {
            binding.tvRead.setText(ex.toString());
            //Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
        }

    }
    public void StartIzmNew() {
        Log.d(TAG, "StartIzm");
        clearChart();
        StopThreadNew();
        iavailable = ftDev.getQueueStatus();
        if(iavailable>0)
            ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));

        if(ftDev.isOpen())
            ftDev.close();
        ftDev = ftdid2xx.openByIndex(DeviceUARTContext, openIndex);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ftDev.restartInTask();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SetSettings();
        ftDev.getQueueStatus();

        try {
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x06;
            int i = ftDev.getQueueStatus();
            ftDev.write(buff, buff.length);
            //ftDev.restartInTask();
            binding.tvRead.setText("StartIzm");
            StartThreadNew(ftDev, false);
        }
        catch (Exception ex) {
            binding.tvRead.setText(ex.toString());
        }
    }

    public void FinishIzmNew(){
        Log.d(TAG, "FinishIzm");
        try {
            StopThreadNew();
            if (ftDev.isOpen() == false) {
                binding.tvRead.setText("SendMessage: device not open");
                return;
            }
            buff = new byte[2];
            buff[0] = (byte) 0xA5;
            buff[1] = 0x02;
            ftDev.write(buff, buff.length);
            binding.tvRead.setText("FinishIzm");
        }
        catch (Exception ex) {
            binding.tvRead.setText(ex.toString());
        }
    }

    @Override
    public void onProgressConclude() {
        //binding.tvCount.setText("c="+Repository.outData.size());
    }

    private void clearChart(){
	    //if(binding.chart.valu)
        //binding.chart.clearValues();
        binding.chart.clear();
    }
    @Override
    public void onPostExecuteConcluded(boolean isSingle) {
        //binding.tvCount.setText("c="+Repository.outData.size());
    }

    @Override
    public void onCanceled() {

    }

    /*@Override
    public void onPostCalculateConcluded(List<Signal> outData) {

    }*/

    @Override
    public void onPostCalculateConcluded(List<Signal> outData, List<Signal> outIntegrateData, float minMaxValue, float minMaxIntegrateValue, float min, float max, float average) {

    }
/*
    @Override
    public void onPostCalculateConcluded(List<Entry> outData) {
        initChart(outData);
        binding.chart.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }*/
}
