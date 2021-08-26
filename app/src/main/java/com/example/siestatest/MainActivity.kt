package com.example.siestatest

import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    private lateinit var scrollView: ScrollView
    private lateinit var panel: RelativeLayout
    private lateinit var panelText: TextView
    private lateinit var theBigText: TextView
    private lateinit var compassIcon: ImageView
    private lateinit var scrollLinearLayout: LinearLayout
    private var maxScroll = 0
    private var scrollY = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()//Убирает верхний бар с названием приложения
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)//Ставит светлую тему по умолчанию
        window.navigationBarColor =
            resources.getColor(R.color.black)//Ставит чёрный цвет навигационной панели
        setContentView(R.layout.activity_main)
        scrollView = findViewById(R.id.scrollView)
        panel = findViewById(R.id.panel)
        panelText = findViewById(R.id.theBigText)
        theBigText = findViewById(R.id.panelText)
        compassIcon = findViewById(R.id.compassIcon)
        scrollLinearLayout = findViewById(R.id.scrollLinearLayout)

        val params =
            panel.layoutParams as ViewGroup.MarginLayoutParams//Высчитывает максимально возможный скролл для огромного текста
        maxScroll = params.topMargin

        for (index in 0..10) {
            addCardToScroll(index.toString())
        }
        scrollView.viewTreeObserver.addOnScrollChangedListener(ViewTreeObserver.OnScrollChangedListener { //Включается когда производится скролл
            scrollEngine()
            alphaElevation()
        })
        touchFun()
    }


    private fun addCardToScroll(_input: String) {
        val blockView = View.inflate(this, R.layout.block, null)//Создаём 1 block
        val blockText = blockView.findViewById<TextView>(R.id.text)//Инициализируем поле Text
        val blockImage = blockView.findViewById<ImageView>(R.id.image)//Инициализируем поле Image
        var isCheck = false
        blockText.text = _input
        blockImage.setImageResource(R.color.white)

        scrollLinearLayout.addView(blockView)//Добавляем block в scrollView

        val params =
            blockView.layoutParams as? ViewGroup.MarginLayoutParams
        params?.setMargins(20, 12, 20, 12)//Устанавливаем Margin
        blockView.layoutParams = params//Присваиваем новые параметры
        blockView.elevation = 20f//Поднимаем карточку вверх для появления тени вокруг
        blockText.setAutoSizeTextTypeUniformWithConfiguration(
            1, 22, 1, TypedValue.COMPLEX_UNIT_DIP)//С помощью кода устанавливаем атрибут AutoSize
        //Присваиваем слушатель
        blockView.setOnClickListener {
            isCheck = !isCheck
            if (isCheck)
                blockImage.setImageResource(R.drawable.checkyes)//Заменяем иконку
            else
                blockImage.setImageResource(R.drawable.checkno)

            animateView(blockImage)//Анимируем иконку
        }
    }

    private fun scrollEngine() {
        val upDown: Boolean = scrollView.scrollY < scrollY// true if scroll up
        val params =
            panel.layoutParams as ViewGroup.MarginLayoutParams//Параметры LinearLayout MiniMain

        val temp: Int = if (upDown) {//Считаем на сколько нужно подвинуть нашу panel
            if (scrollView.scrollY <= maxScroll) {
                params.topMargin + (scrollY - scrollView.scrollY)
            } else
                0
        } else
            params.topMargin - scrollView.scrollY + scrollY
        if ((temp < 0) && !(upDown)) {//Двигаем panel в зависимости от прокрутки
            params.topMargin = 0
        } else if ((temp > maxScroll) && (upDown)) {
            params.topMargin = maxScroll
        } else {
            params.topMargin = temp
        }

        panel.layoutParams = params//Присваиваем изменения
        scrollY = scrollView.scrollY
    }

    private fun alphaElevation() {
        val params =
            panel.layoutParams as ViewGroup.MarginLayoutParams
        panelText.alpha =
            (1 - (scrollY.toFloat() * 100.0 / maxScroll.toFloat() / 100.0) / 0.5).toFloat()//Плавное исчезновение/появление panelText
        theBigText.alpha = (scrollY.toFloat() * 100.0 / maxScroll.toFloat() / 100.0).toFloat()//Плавное исчезновение/появление большого текста
//Если вдруг захотите, чтобы иконка тоже появлялась плавно, раскомментируете данный участок
//        var settingsAlpha =
//            ((scrollY.toFloat() * 100.0 / maxScroll.toFloat() / 100.0)).toFloat()
//        if (settingsAlpha < 0.7f)
//            CompasIcon.alpha = settingsAlpha
//        else
//            CompasIcon.alpha = 0.7f
        //Если panel достигла верхнего края экрана -> добавить тень
        if (params.topMargin == 0)
            panel.elevation = 10f
        else
            panel.elevation = 0.1f
    }

    //Проверка нажатия на экран
    @SuppressLint("ClickableViewAccessibility")
    fun touchFun() {
        scrollView.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    false
                }

                MotionEvent.ACTION_UP -> {
                    threadTimer()
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    false
                }
                MotionEvent.ACTION_OUTSIDE -> {
                    false
                }
                else -> false
            }
        }
    }

    //Таймер+приведение panel в стандартное положение
    private fun threadTimer() {
        var lastScrollY = 0
        //Мы не имеем права использовать заморозки в главном UI потоке, поэтому вынуждены создать новый
        Thread {
            //Пока новое положение ScrollView не сравнится со старым
            while (scrollView.scaleY.toInt() != lastScrollY) {
                lastScrollY = scrollView.scrollY
                //Пол секунды
                Thread.sleep(500)
                //Если текущее положение не равняется предыдущему
                if (scrollView.scaleY.toInt() != lastScrollY)
                    lastScrollY = scrollView.scaleY.toInt()
            }
            //Если положение ScrollView меньше, чем максимальное положение panel
            if (scrollY < maxScroll) {
                //Елси ScrollView ближе к максимальному значению panel
                if (scrollY >= maxScroll / 3)//Плавный скролл
                    //Так как мы не можем изменять UI не из главного потока, мы используем post
                    scrollView.post { scrollView.smoothScrollTo(0, maxScroll) }
                //Если ScrollView ближе к верхнему краю экрана
                else if (scrollY < maxScroll / 3)
                //Так как мы не можем изменять UI не из главного потока, мы используем post
                    scrollView.post { scrollView.smoothScrollTo(0, 0) }
            }
        }.start()
    }


    //Анимация иконок
    private fun animateView(view: ImageView) {
        when (val drawable = view.drawable) {
            is AnimatedVectorDrawable -> {
                drawable.start()
            }
        }
    }
}