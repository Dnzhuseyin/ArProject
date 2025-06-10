package com.example.arproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.SphereNode

class ArActivity : AppCompatActivity() {

    private lateinit var arScene: ArSceneView
    private lateinit var statusText: TextView
    private var modelNode: ArModelNode? = null
    
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
    private var currentColor = Float3(0.0f, 0.8f, 0.8f) // Varsayılan: Turkuaz mavi
    private var shapeNode: Node? = null
    
    // İzin kodu
    private val CAMERA_PERMISSION_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        
        // UI bileşenlerini bulma
        arScene = findViewById(R.id.arSceneView)
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
            currentColor = Float3(1.0f, 0.0f, 0.0f) // Kırmızı
            updateModel()
            Toast.makeText(this, "Kırmızı renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        greenButton.setOnClickListener {
            currentColor = Float3(0.0f, 1.0f, 0.0f) // Yeşil
            updateModel()
            Toast.makeText(this, "Yeşil renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        blueButton.setOnClickListener {
            currentColor = Float3(0.0f, 0.0f, 1.0f) // Mavi
            updateModel()
            Toast.makeText(this, "Mavi renk seçildi", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupAr() {
        // Durum metnini güncelle
        updateStatusText("Yüzeyleri arıyor...")
        
        // AR modelini oluşturma
        createModelNode()
        
        // Yüzey tespiti için callback
        arScene.onArFrame = { frame ->
            val trackables = frame.session.getAllTrackables(Plane::class.java)
            val hasPlanes = trackables.any { it.trackingState == TrackingState.TRACKING }
            
            if (hasPlanes) {
                updateStatusText("Yüzey bulundu. Yerleştirmek için dokunun.")
            } else {
                updateStatusText("Yüzeyleri arıyor...")
            }
        }
        
        // Dokunma işleyici
        arScene.onArTap = { hitResult, _, _ ->
            hitResult?.let { hit ->
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Yüzeye model yerleştir
                    placeModel(hit.createAnchor())
                    return@onArTap true
                }
            }
            false
        }
    }
    
    private fun placeModel(anchor: Anchor) {
        // Önceki anchor'u temizle
        modelNode?.anchor = null
        
        // Eğer model yoksa oluştur
        if (modelNode == null) {
            modelNode = ArModelNode(arScene.engine).apply {
                placementMode = PlacementMode.INSTANT
                this.anchor = anchor
                // Şekli çiz
                updateModel()
                arScene.addChild(this)
            }
        } else {
            // Var olan modeli güncelle
            modelNode?.apply {
                this.anchor = anchor
                updateModel()
            }
        }
        
        val shapeName = when(currentShape) {
            ShapeType.CUBE -> "Küp"
            ShapeType.SPHERE -> "Küre"
            ShapeType.CYLINDER -> "Silindir"
        }
        
        Toast.makeText(this, "$shapeName yerleştirildi!", Toast.LENGTH_SHORT).show()
    }
    
    private fun createModelNode() {
        modelNode = ArModelNode(arScene.engine).apply {
            placementMode = PlacementMode.DISABLED
            isVisible = true
        }
    }
    
    private fun updateModel() {
        // Eğer model node varsa ve görünürse şekili güncelle
        modelNode?.let { node ->
            // Önceki şekli kaldır
            shapeNode?.let { node.removeChild(it) }
            
            // Yeni şekli ekle
            shapeNode = when (currentShape) {
                ShapeType.CUBE -> createCube()
                ShapeType.SPHERE -> createSphere()
                ShapeType.CYLINDER -> createCylinder()
            }
            
            shapeNode?.let { node.addChild(it) }
        }
    }
    
    private fun createCube(): Node {
        return CubeNode(arScene.engine).apply {
            position = dev.romainguy.kotlin.math.Float3(0f, 0f, 0f)
            scale = Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun createSphere(): Node {
        return SphereNode(arScene.engine).apply {
            position = dev.romainguy.kotlin.math.Float3(0f, 0f, 0f)
            scale = Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun createCylinder(): Node {
        return CylinderNode(arScene.engine).apply {
            position = dev.romainguy.kotlin.math.Float3(0f, 0f, 0f)
            scale = Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun updateStatusText(text: String) {
        runOnUiThread {
            statusText.text = text
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAr()
            } else {
                Toast.makeText(
                    this,
                    "Kamera izni reddedildi. AR fonksiyonu çalışmayacak.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
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
            currentColor = Float3(1.0f, 0.0f, 0.0f) // Kırmızı
            updateModel()
            Toast.makeText(this, "Kırmızı renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        greenButton.setOnClickListener {
            currentColor = Float3(0.0f, 1.0f, 0.0f) // Yeşil
            updateModel()
            Toast.makeText(this, "Yeşil renk seçildi", Toast.LENGTH_SHORT).show()
        }
        
        blueButton.setOnClickListener {
            currentColor = Float3(0.0f, 0.0f, 1.0f) // Mavi
            updateModel()
            Toast.makeText(this, "Mavi renk seçildi", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupAr() {
        // Durum metnini güncelle
        updateStatusText("Yüzeyleri arıyor...")
        
        // AR modelini oluşturma
        createModelNode()
        
        // Yüzey tespiti için callback
        arScene.onArFrame = { frame ->
            val trackables = frame.session.getAllTrackables(Plane::class.java)
            val hasPlanes = trackables.any { it.trackingState == com.google.ar.core.TrackingState.TRACKING }
            
            if (hasPlanes) {
                updateStatusText("Yüzey bulundu. Yerleştirmek için dokunun.")
            } else {
                updateStatusText("Yüzeyleri arıyor...")
            }
        }
        
        // Dokunma işleyici
        arScene.onArTap = { hitResult, _, _ ->
            hitResult?.let { hit ->
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Yüzeye model yerleştir
                    placeModel(hit.createAnchor())
                    return@onArTap true
                }
            }
            false
        }
    }
    
    private fun placeModel(anchor: Anchor) {
        // Önceki anchor'u temizle
        modelNode?.anchor = null
        
        // Eğer model yoksa oluştur
        if (modelNode == null) {
            modelNode = ArModelNode(arScene.engine).apply {
                placementMode = PlacementMode.INSTANT
                this.anchor = anchor
                // Şekli çiz
                updateModel()
                arScene.addChild(this)
            }
        } else {
            // Var olan modeli güncelle
            modelNode?.apply {
                this.anchor = anchor
                updateModel()
            }
        }
        
        val shapeName = when(currentShape) {
            ShapeType.CUBE -> "Küp"
            ShapeType.SPHERE -> "Küre"
            ShapeType.CYLINDER -> "Silindir"
        }
        
        Toast.makeText(this, "$shapeName yerleştirildi!", Toast.LENGTH_SHORT).show()
    }
    
    private fun createModelNode() {
        modelNode = ArModelNode(arScene.engine).apply {
            placementMode = PlacementMode.DISABLED
            isVisible = true
        }
    }
    
    private fun updateModel() {
        // Eğer model node varsa ve görünürse şekili güncelle
        modelNode?.let { node ->
            // Önceki şekli kaldır
            shapeNode?.let { node.removeChild(it) }
            
            // Yeni şekli ekle
            shapeNode = when (currentShape) {
                ShapeType.CUBE -> createCube()
                ShapeType.SPHERE -> createSphere()
                ShapeType.CYLINDER -> createCylinder()
            }
            
            shapeNode?.let { node.addChild(it) }
        }
    }
    
    private fun createCube(): Node {
        return CubeNode(arScene.engine).apply {
            position = Position(0f, 0f, 0f)
            scale = dev.romainguy.kotlin.math.Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun createSphere(): Node {
        return SphereNode(arScene.engine).apply {
            position = Position(0f, 0f, 0f)
            scale = dev.romainguy.kotlin.math.Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun createCylinder(): Node {
        return CylinderNode(arScene.engine).apply {
            position = Position(0f, 0f, 0f)
            scale = dev.romainguy.kotlin.math.Float3(0.2f, 0.2f, 0.2f)
            setColor(currentColor)
        }
    }
    
    private fun updateStatusText(text: String) {
        runOnUiThread {
            statusText.text = text
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAr()
            } else {
                Toast.makeText(
                    this,
                    "Kamera izni reddedildi. AR fonksiyonu çalışmayacak.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
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
