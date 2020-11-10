package com.example.fmkmeter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.databinding.DataBindingUtil;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.fmkmeter.databinding.FragmentMeterBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MeterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public /*abstract*/ class MeterFragment<V extends MeterContractor.View, P extends MeterContractor.Presenter<V>> extends Fragment
        implements MeterContractor.View {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "MeterFragment";
    protected MeterContractor.Presenter presenter;
    private FragmentMeterBinding binding;
    private OnFragmentInteractionListener mListener;
    ArrayAdapter<String> adapter;
    private List<String> spinnerListDevices = new ArrayList<String>();
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public MeterFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MeterFragment newInstance(/*String param1, String param2*/) {
        MeterFragment fragment = new MeterFragment();
        Bundle args = new Bundle();
        /*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int izm;
        if (getArguments() != null) {
            izm = getArguments().getInt("isStartIzm");
            Log.d("onCreate", "" + izm);
            /*mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);*/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meter, container, false);
        View view = ((ViewDataBinding) binding).getRoot();
        initListeners();
        MeterViewModel viewModel =
                ViewModelProviders.of(getActivity()).get(MeterViewModel.class);
        if (viewModel.getPresenter() == null) {
            viewModel.setPresenter(new MeterPresenter());
        }
        presenter = viewModel.getPresenter();
        presenter.attachLifecycle(getLifecycle());
        presenter.attachView((V) this);
        presenter.setRepository(viewModel.getRepository(), this);
        spinnerListDevices = presenter.getListDevices();
        setSpinners();
        setHasOptionsMenu(true);
        int isStartIzm = MeterFragmentArgs.fromBundle(getArguments()).getIsStartIzm();
        if (isStartIzm == 1)
            presenter.startIzmOnClick();
        //return inflater.inflate(R.layout.fragment_meter, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_fragment_settings:
                //NavHostFragment navHostFragment =  NavHostFragment.findNavController(this);
                NavController navController = NavHostFragment.findNavController(this);
                NavigationUI.onNavDestinationSelected(item, navController);
                //NavController navController = NavHostFragment.findNavController(R.id.nav_host_fragment);
                //item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
                break;
            case R.id.menu_info:
                presenter.getInfoDialog().show(getFragmentManager(), TAG);
                break;
            case R.id.menu_refresh:
                adapter.clear();
                presenter.refreshOnClick();
                adapter.notifyDataSetChanged();
                break;
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }*/

    @Override
    public void onStop() {
        super.onStop();
        presenter.detachLifecycle(getLifecycle());
        presenter.detachView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void initListeners() {
        //binding.tvRead.setMovementMethod(new ScrollingMovementMethod());
        binding.btnResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                Navigation.findNavController(v).navigate(R.id.action_meterFragment_to_chartFragment);
                //presenter.resultOnClick();
            }
        });
        binding.btnStartIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startIzmOnClick();
                //presenter.startIzmOnClickTest();
                //StartIzmNew();
            }
        });
        binding.btnFinishIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.finishIzmOnClick();
                //presenter.finishIzmOnClickTest();
                //FinishIzmNew();
            }
        });
        binding.btnSingleIzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.singlIzmOnClick();
                //SingleIzmNew();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        getActivity().getApplicationContext().registerReceiver(mUsbPlugEvents, filter);

        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().getApplicationContext().registerReceiver(mUsbReceiver, filter);
    /*   UsbManager mUsbManager = (UsbManager)getActivity().getApplicationContext().getSystemService(Context.USB_SERVICE);

...
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mPermissionIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().getApplicationContext().registerReceiver(mUsbPlugEvents, filter);*/
        //registerReceiver(mUsbReceiver, filter);
    }

    private void setSpinners() {
        adapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerListDevices);
        binding.spnDevices.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spnDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("onItemSelected", view.toString());
                presenter.spnDevicesItemOnClick(position);
                //ConnectDevice(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Log.v("onNothingSelected", parent.toString());
            }
        });

        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.array_si, android.R.layout.simple_spinner_dropdown_item);
        binding.spnSi.setAdapter(adapter1);
        binding.spnSi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("onItemSelected", view.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), InfoDialogFragment.TAG);
    }

    @Override
    public void updateCounter(int cnt) {
        binding.tvCountByteAll.setText("byteAll=" + cnt);
    }

    @Override
    public void updateResult(int cnt) {
        binding.tvCountByteResult.setText("byteRes=" + cnt);
    }

    @Override
    public void showProgressBar(boolean tf) {
        if (tf)
            binding.progressBar.setVisibility(View.VISIBLE);
        else
            binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void selectSpnDevice(int index) {
        binding.spnDevices.setSelection(index);
        binding.spnDevices.getSelectedItem();
    }

    @Override
    public void goToResult() {
        Navigation.findNavController(binding.btnResult).navigate(R.id.action_meterFragment_to_chartFragment);
    }

    @Override
    public void izmIsStart(boolean isStart) {
        if (isStart) {
            binding.btnStartIzm.setVisibility(View.GONE);
            binding.btnFinishIzm.setVisibility(View.VISIBLE);
        } else {
            binding.btnStartIzm.setVisibility(View.VISIBLE);
            binding.btnFinishIzm.setVisibility(View.GONE);

        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver mUsbPlugEvents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                //Device was attached, ask for permission
                UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                usbManager.requestPermission(device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                //Disconnected
                //UsbMonitorConnection.disconnect();
            }

          /* if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                try {
                    //spinnerListDevices = presenter.getListDevices();
                    //presenter.refreshOnClick();
                    //adapter.notifyDataSetChanged();
                    UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    usbManager.requestPermission(device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //binding.tvRead.setText(s);
                        //error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                try {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    //InitDeviceList();
                    //ConnectDevice(openIndex);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    String s = e.getMessage();
                    if (s != null) {
                        //binding.tvRead.setText(s);
                        //error_Information.setText(s);
                    }
                    e.printStackTrace();
                }
            }*/
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        && device != null) {
                    presenter.refreshOnClick();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "permission succes for device " + device);
                    //getDeviceStatus(device);
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
            }
        }
    };
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
