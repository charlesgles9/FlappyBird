package com.evolution.flappybird.entities

import com.graphics.glcanvas.engine.Batch
import com.graphics.glcanvas.engine.Update
import com.graphics.glcanvas.engine.maths.ColorRGBA
import com.graphics.glcanvas.engine.maths.Vector2f
import com.graphics.glcanvas.engine.structures.RectF
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class Pillars(val start:Vector2f, val maxDistance:Vector2f, val verticalSpacing:Float,val horizontalSpacing:Float, val maxWidth:Float, val maxHeight:Float):Update{

   val bars= mutableListOf<Pair<RectF,RectF>>()
  
    init {

       reset()

    }

    fun reset(){
        bars.clear()
        val count=(maxDistance.x/(maxWidth+verticalSpacing)).toInt()
        for(i in 0 until count){
            val x=start.x+(i+1)*(verticalSpacing+maxWidth)
            val centerSpaceSize=horizontalSpacing+getRandom(horizontalSpacing)
            val pillarHeight1=Random.nextFloat()*(maxHeight-centerSpaceSize)
            val pillarHeight2= maxHeight-pillarHeight1- centerSpaceSize
            val topY=start.y+pillarHeight1*0.5f
            val bottomY=start.y-pillarHeight2*0.5f+maxHeight
            val topPillar=RectF(x,topY,maxWidth,pillarHeight1)
            val bottomPillar=RectF(x,bottomY,maxWidth,pillarHeight2)
            val topColor=ColorRGBA(ColorRGBA.cyan)
            val bottomColor=ColorRGBA(ColorRGBA.red)
            topPillar.setColor(ColorRGBA.darken(0.7f,topColor))
            bottomPillar.setColor(ColorRGBA.darken(0.7f,bottomColor))
            bars.add(Pair(topPillar,bottomPillar))
        }
    }

    fun closest(bird: Bird, pillar: RectF):Double{

        return (sqrt((bird.getX() - pillar.getX()).pow(2f)+(bird.getY()-pillar.getY()).pow(2f)) /pillar.getY()).toDouble()
    }


    private fun getRandom(seed:Float):Float{
        return Random.nextFloat()*seed;
    }

    override fun draw(batch: Batch) {
       bars.forEach { bar->
           batch.draw(bar.first)
           batch.draw(bar.second)
       }

    }

    override fun update(delta: Long) {

    }

}