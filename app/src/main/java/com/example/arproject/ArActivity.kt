package com.example.arproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.android.filament.utils.Float3
import com.google.android.filament.Color
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.SphereNode
import io.github.sceneview.node.CylinderNode

class ArActivity : AppCompatActivity() {

    private lateinit var arSceneView: ArSceneView
    private lateinit var statusText: TextView
    private lateinit var modelNode: ArModelNode
    
    // Şekil ve renk kontrolleri
    private lateinit var cubeButton: Button
    private lateinit var sphereButton: Button
    private lateinit var cylinderButton: Button
    private lateinit var redButton: Button
    private lateinit var greenButton: Button
    private lateinit var blueButton: Button
    
    // Şekil türü ve renk için enum sınıfları
    private enum class ShapeType { CUBE, SPHERE, CYLINDER }
    
    // Geçerli şekil ve renk değerleri
    private var currentShape = ShapeType.CUBE
    private var currentColor = Color(0.0f, 0.8f, 0.8f, 1.0f) // Varsayılan: Turkuaz mavi
    
    // İzin kodu
    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        
        // UI bileşenlerini bulma
        arSceneView = findViewById(R.id.arSceneView)
        statusText = findViewById(R.id.statusText)
        
        // Butonları bağla
        initializeButtons()
        
        // Kamera izni kontrolü
        if (!checkCameraPermission()) {
            requestCameraPermission()
        } else {
            setupAr()
        }
    }
    
    private fun initializeButtons() {
        // Şekil butonları
        cubeButton = findViewById(R.id.cubeButton)
        sphereButton = findViewById(R.id.sphereButton)
        cylinderButton = findViewById(R.id.cylinderButton)
        
        // Renk butonları
        redButton = findViewById(R.id.redButton)
        greenButton = findViewById(R.id.greenButton)
        blueButton = findViewById(R.id.blueButton)
        
        // Şekil butonları için tıklama olayları
        cubeButton.setOnClickListener {
            currentShape = ShapeType.CUBE
            updateModel()
            Toast.makeText(this, "Küp şekli seçildi", Toast.LENGTH_SHORT).show()
        }
        
        sphereButton.setOnClickListener {
            currentShape = ShapeType.SPHERE
            updateModel()
            Toast.makeText(this, "Küre şekli seçildi", Toast.LENGTH_SHORT).show()
        }
        
        cylinderButton.setOnClickListener {
            currentShape = ShapeType.CYLINDER
            updateModel()
            Toast.makeText(this, "Silindir şekli seçildi", Toast.LENGTH_SHORT).show()
        }
        
        // Renk butonları için tıklama olayları
        redButton.setOnClickListener {
            currentColor = Color(1.0f, 0.0f, 0.0f, 1.0f) // Kırmızı
            updateModel()
            Toast.makeText(this, "Kırmızı renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        greenButton.setOnClickListener {
            currentColor = Color(0.0f, 1.0f, 0.0f, 1.0f) // Yeşil
            updateModel()
            Toast.makeText(this, "Yeşil renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        blueButton.setOnClickListener {
            currentColor = Color(0.0f, 0.0f, 1.0f, 1.0f) // Mavi
            updateModel()
            Toast.makeText(this, "Mavi renk seçildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAr() {
        // AR Modunu ve yüzey algılamayı yapılandırma
        arSceneView.enableARCore = true
        arSceneView.planeRenderer.isEnabled = true
        arSceneView.planeRenderer.isShadowReceiver = true
        
        // 3D model nodunu oluşturma
        createModelNode()
        
        // Model nodunu scene'e ekle (başlangıçta görünmez olacak)
        arSceneView.addChild(modelNode)

        // Durum metni güncelleme
        updateStatusText()

        // Dokunma dinleyicisini ayarlama
        arSceneView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Ekrandaki dokunma noktasını kullanarak AR düzlemlerini test etme
                val hitResults = arSceneView.hitTest(event.x, event.y, true, false)
                
                // Geçerli bir düzlem bul
                hitResults.firstOrNull { hit ->
                    // Sadece yatay düzlemler üzerinde çalış
                    val trackable = hit.trackable
                    trackable is Plane && trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                }?.let { hit ->
                    // Önceki modelimizi temizle
                    modelNode.detachAnchor()
                    
                    // Yeni anchor oluştur ve modeli yerleştir
                    hit.createAnchor()?.let { anchor ->
                        modelNode.anchor = anchor
                        
                        // Başarılı yerleştirme için bildirim göster
                        val shapeName = when(currentShape) {
                            ShapeType.CUBE -> "Küp"
                            ShapeType.SPHERE -> "Küre"
                            ShapeType.CYLINDER -> "Silindir"
                        }
                        Toast.makeText(this, "$shapeName yerleştirildi!", Toast.LENGTH_SHORT).show()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
    
    /**
     * Model nodunu oluştur veya güncelle
     */
    private fun createModelNode() {
        modelNode = ArModelNode(PlacementMode.PLANE_HORIZONTAL).apply {
            // Geçerli şekle göre modeli yükle
            when(currentShape) {
                ShapeType.CUBE -> {
                    val cubeNode = CubeNode(
                        size = 0.2f,
                        center = Position(0.0f, 0.1f, 0.0f),
                        color = currentColor
                    )
                    addChild(cubeNode)
                }
                ShapeType.SPHERE -> {
                    val sphereNode = SphereNode(
                        radius = 0.15f,
                        center = Position(0.0f, 0.15f, 0.0f),
                        color = currentColor
                    )
                    addChild(sphereNode)
                }
                ShapeType.CYLINDER -> {
                    val cylinderNode = CylinderNode(
                        radius = 0.1f,
                        height = 0.25f,
                        center = Position(0.0f, 0.125f, 0.0f),
                        color = currentColor
                    )
                    addChild(cylinderNode)
                }
            }
            
            // Model ölçeği ve görünürlüğü
            scale = Position(0.5f, 0.5f, 0.5f)
            isVisible = false
            
            // Model yerleştirildiğinde
            onAnchorChanged = { anchor ->
                isVisible = anchor != null
                updateStatusText(anchor != null)
            }
        }
    }
    
    /**
     * Seçilen şekil veya renk değiştiğinde modeli günceller
     */
    private fun updateModel() {
        // Mevcut anchor'u koru
        val currentAnchor = modelNode.anchor
        
        // Önceki tüm çocuk nodları temizle
        modelNode.children.toList().forEach { child ->
            modelNode.removeChild(child)
        }
        
        // Seçilen şekil türüne göre yeni şekil ekle
        when(currentShape) {
            ShapeType.CUBE -> {
                val cubeNode = CubeNode(
                    size = 0.2f,
                    center = Position(0.0f, 0.1f, 0.0f),
                    color = currentColor
                )
                modelNode.addChild(cubeNode)
            }
            ShapeType.SPHERE -> {
                val sphereNode = SphereNode(
                    radius = 0.15f,
                    center = Position(0.0f, 0.15f, 0.0f),
                    color = currentColor
                )
                modelNode.addChild(sphereNode)
            }
            ShapeType.CYLINDER -> {
                val cylinderNode = CylinderNode(
                    radius = 0.1f,
                    height = 0.25f,
                    center = Position(0.0f, 0.125f, 0.0f),
                    color = currentColor
                )
                modelNode.addChild(cylinderNode)
            }
        }
    }
    
    /**
     * Durum metnini günceller
     */
    private fun updateStatusText(isPlaced: Boolean = false) {
        statusText.text = if (isPlaced) {
            "Model yerleştirildi! Yeni bir model yerleştirmek için tekrar dokunun"
        } else {
            val shapeName = when(currentShape) {
                ShapeType.CUBE -> "küp"
                ShapeType.SPHERE -> "küre"
                ShapeType.CYLINDER -> "silindir"
            }
            "Yüzeyi tarayın ve $shapeName yerleştirmek için dokunun"
        }
    }

    // Kamera izni kontrolü
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Kamera izni isteme
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    // İzin sonucunu işleme
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                setupAr()
            } else {
                // İzin reddedildi
                Toast.makeText(
                    this,
                    "Bu uygulama için kamera izni gereklidir!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
