package com.evolution.flappybird.entities

import com.evolution.flappybird.ai.NeuralNetwork
import com.graphics.glcanvas.engine.Batch
import com.graphics.glcanvas.engine.maths.ColorRGBA
import com.graphics.glcanvas.engine.maths.Vector2f
import com.graphics.glcanvas.engine.structures.RectF
import com.graphics.glcanvas.engine.utils.AnimationFrame
import com.graphics.glcanvas.engine.utils.SpriteAnimator
import com.graphics.glcanvas.engine.utils.SpriteSheet
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class Bird(x:Float,y:Float,width:Float,height:Float,val srcW:Float,val srcH:Float):RectF(x,y,width, height){
    private var velocity=Vector2f(2.9f,0f)
    private var gravity=Vector2f(0f,6.9f)
    private var flapVelocity=14f
    private var friction=0.92f
    var score=0f
    var alive=true
    var network= NeuralNetwork(4,8,1)
    var angle=0f

    init {
        setSpriteSheet(SpriteSheet(4,1))
        getSpriteSheet().setCurrentFrame(0)
        setAnimator(SpriteAnimator("fly", AnimationFrame(50L,4,1),getSpriteSheet()))
        getAnimator()?.put("idle", AnimationFrame(100L,1,1),getSpriteSheet())
        getAnimator()?.setActivated(true)
        getAnimator()?.setLooping(true)
        getAnimator()?.setCurrent("idle")
        gradient(randomGradient())


    }
    constructor(x:Float,y:Float,width:Float,height:Float, srcW:Float, srcH:Float,network: NeuralNetwork):this(x, y, width, height, srcW, srcH){
        this.network=network

    }

    // keep track of the birds based on color
    private fun randomColor():ColorRGBA{
        return ColorRGBA(Random.nextFloat(), Random.nextFloat(),Random.nextFloat(),1f)
    }

    private fun randomGradient():MutableList<ColorRGBA>{

        return mutableListOf(randomColor(),randomColor(),randomColor(),randomColor())
    }

    fun draw(batch: Batch) {

        if(alive)
        batch.draw(this)
    }

    private fun flap(){
        velocity.y=-flapVelocity
    }

    fun reset(){
        set(100.0f,srcH*0.5f)
    }

    @Override
    fun update( time:Long,closest:Pair<RectF,RectF>){


        getAnimator()?.update(time)
        if(!alive) {
            reset()
            return
        }

         velocity.y*= friction


        //in case this bird brain hits the ground or the top of the world view
        if(getY()>=srcH-getHeight()||getY()<=getHeight()){
            score=getX()
            reset()
            alive=false
            return
        }

        val vy=velocity.y+gravity.y
        angle = if(vy<0){
            abs(vy*3f)
        }else{
            -gravity.y*3f
        }

        set(getX()+velocity.x,getY()+vy)
        // horizontal distance between the bird and the bottom or top pillar
        val nearest1= sqrt ((closest.second.getX()-getX()).pow(2f)+(closest.second.getY()-getY()).pow(2f)).toDouble() /srcW
        // y Position of the bird
        val yPosition=((getY())/srcH).toDouble()
        //difference between the bottom and the top pillar
        val distance3=abs((closest.second.getHeight()-closest.first.getHeight())).toDouble()
        //the center position of first and second pillar
        val center=(closest.first.getHeight()*0.5+closest.first.getY()+distance3*0.5)/srcH
        //the lowest center position between the first and second pillar
        val yDiff1=getY()-(closest.first.getHeight()*0.5+closest.first.getY()+distance3*0.5)
        // the highest position between the second and the first pillar
        val yDiff2=getY()-(closest.second.getY()-closest.second.getHeight()*0.5-distance3*0.5)
        //make a prediction
        val output= network.predict(mutableListOf(center,yPosition,yDiff1,yDiff2))
        val flapValue=output[0]

         if(flapValue>0.5) {
            flap()
            getAnimator()?.setCurrent("fly")
        } else {
            getAnimator()?.setCurrent("idle")
        }
         setRotationZ(angle)

    }

}