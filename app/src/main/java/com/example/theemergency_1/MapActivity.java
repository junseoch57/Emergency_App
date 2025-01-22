package com.example.theemergency_1;

// 필요한 import 추가
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // 위치 권한 요청
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // FusedLocationProviderClient 객체
    private FusedLocationProviderClient fusedLocationClient;

    // 경찰서 위치 리스트
    private List<PoliceStation> policeStations;

    // 성범죄자 위치 리스트
    private List<LatLng> sexOffenders;

    // 마커 및 원 객체 리스트
    private List<com.google.android.gms.maps.model.Marker> policeMarkers = new ArrayList<>();
    private List<Circle> sexOffenderCircles = new ArrayList<>();

    private Button btnPoliceStations;
    private Button btnSexOffenders;

    // 선택된 경찰서 마커
    private com.google.android.gms.maps.model.Marker selectedPoliceMarker;

    // Bottom Sheet (밑에 스크롤 그거)
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private RecyclerView recyclerViewPoliceStations;
    private PoliceStationAdapter policeStationAdapter;

    // 성범죄자 -> 원 표시 상태
    private boolean areSexOffendersShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); // 수정된 부분

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 경찰서 위치 초기화 (실제 위치로 변경)
        policeStations = new ArrayList<>();
        policeStations.add(new PoliceStation(
                "문성 파출소",
                "충남 천안시 동남구 옛시청길 21",
                new LatLng(36.80698824114694, 127.14955536794761)
        ));

        policeStations.add(new PoliceStation(
                "성정 지구대",
                "충남 천안시 서북구 천안천4길 48 성정 지구대",
                new LatLng(36.81029213778365, 127.14484377682342)
        ));

        policeStations.add(new PoliceStation(
                "신안 파출소",
                "충남 천안시 동남구 중앙로 217",
                new LatLng(36.824970137011086, 127.15975115950256)
        ));

        policeStations.add(new PoliceStation(
                "두정 지구대",
                "충남 천안시 서북구 오성9길 40",
                new LatLng(36.833186285687084, 127.13195895977117)
        ));

        // 성범죄자 위치 초기화 (실제 위치로 변경)
        sexOffenders = new ArrayList<>();
        sexOffenders.add(new LatLng(36.818522896739275, 127.15960886869684)); // 성범죄자 위치 예시
        sexOffenders.add(new LatLng(36.83169927855781, 127.14377155013678)); // 추가 성범죄자 위치

        // 버튼 초기화
        btnPoliceStations = findViewById(R.id.btnPoliceStations);
        btnSexOffenders = findViewById(R.id.btnSexOffenders);

        // 지도 fragment 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Bottom Sheet 초기화
        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // RecyclerView 초기화
        recyclerViewPoliceStations = findViewById(R.id.recyclerViewPoliceStations);
        recyclerViewPoliceStations.setLayoutManager(new LinearLayoutManager(this));
        policeStationAdapter = new PoliceStationAdapter(policeStations, new PoliceStationAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(PoliceStation station) {
                // 클릭한 시점으로 이동함
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(station.getLocation(), 15)); // 15 만큼 시점 확대

                // 기존 마커 제거
                if (selectedPoliceMarker != null) {
                    selectedPoliceMarker.remove();
                }

                // 선택된 경찰서에 마커 추가
                selectedPoliceMarker = mMap.addMarker(new MarkerOptions()
                        .position(station.getLocation())
                        .title(station.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // 빨간색 마커 사용
                );

                // BottomSheet 숨기기
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        recyclerViewPoliceStations.setAdapter(policeStationAdapter);

        // 버튼 클릭 이벤트 설정
        btnPoliceStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBottomSheet();
            }
        });

        btnSexOffenders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSexOffenders();
                if (areSexOffendersShown) {
                    btnSexOffenders.setText("성범죄자 위치 숨기기");
                } else {
                    btnSexOffenders.setText("성범죄자 위치 보기");
                }
            }
        });

        // 위치 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 허용되지 않은 경우, 권한 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 위치 로딩
            getLastLocation();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 지도 타입 설정
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 마커 클릭 리스너 이벤트
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
                // 마커 클릭 시 동작 정의
                Toast.makeText(MapActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // 위치 권한이 허용된 경우, 내 위치 표시
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // 모든 경찰서에 마커로 한 번에 표시함
        for (PoliceStation station : policeStations) {
            mMap.addMarker(new MarkerOptions()
                    .position(station.getLocation())
                    .title(station.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
        }
    }

    // 위치 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // 요청한 권한이 허용 됐는지 확인
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 내 위치 표시하고 현재 위치로 이동함
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getLastLocation();
                }
            } else {
                // 권한 거부 됐을 경우
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 마지막 위치 가져오기
    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new com.google.android.gms.tasks.OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // 위치가 null이 아닌 경우, 지도 카메라 이동
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                            // 현재 위치에 마커 추가
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .title("현재 위치")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        } else {
                            // 위치가 null인 경우, 기본 위치로 설정
                            LatLng defaultLatLng = new LatLng(37.5665, 126.9780); // 임의로 서울역 위치로 표시
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10));
                            // zoom 10 : 시점 확대
                            // zoom 2 : 시점 축소
                            mMap.addMarker(new MarkerOptions()
                                    .position(defaultLatLng)
                                    .title("서울")
                                    .snippet("한국의 수도"));
                            Toast.makeText(MapActivity.this, "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Bottom Sheet 토글 함수
    private void toggleBottomSheet() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    // 성범죄자 위치 표시 및 숨기기 함수
    private void showSexOffenders() {
        if (areSexOffendersShown) {
            // 원이 이미 표시되고 있으면 제거
            for (Circle circle : sexOffenderCircles) {
                circle.remove();
            }
            sexOffenderCircles.clear();
            areSexOffendersShown = false;

        } else {

            // 원이 표시되고 있지 않으면 추가
            for (LatLng offender : sexOffenders) {
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(offender)
                        .radius(500) // 500미터짜리 원 생성
                        .strokeColor(0xFFFF0000) // 빨간색 테두리
                        .fillColor(0x44FF0000)   // 반투명 빨간색 채우기
                        .clickable(false)
                );
                sexOffenderCircles.add(circle);
            }
            areSexOffendersShown = true;
        }
    }
}
