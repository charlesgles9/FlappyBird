package com.evolution.flappybird.entities

import com.evolution.flappybird.ai.NeuralNetwork
import com.graphics.glcanvas.engine.Batch
import com.graphics.glcanvas.engine.maths.Vector2f
import com.graphics.glcanvas.engine.structures.RectF
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Bird(x:Float,y:Float,width:Float,height:Float,val srcW:Float,val srcH:Float):RectF(x,y,width, height){
    private var velocity=Vector2f(2.9f,0f)
    private var gravity=Vector2f(0f,6.9f)
    private var flapVelocity=14f
    private var friction=0.92f
    var score=0f
    var alive=true
    var network= NeuralNetwork(4,8,1)
    constructor(x:Float,y:Float,width:Float,height:Float, srcW:Float, srcH:Float,network: NeuralNetwork):this(x, y, width, height, srcW, srcH){
        this.network=network
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

    fun update(closest:Pair<RectF,RectF>){
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
        set(getX()+velocity.x,getY()+velocity.y+gravity.y)
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

        if(flapValue>0.5)
            flap()

    }

}