package com.evolution.flappybird.ai

import com.graphics.glcanvas.engine.Batch
import com.graphics.glcanvas.engine.Update
import com.graphics.glcanvas.engine.structures.Circle
import kotlin.math.min
import kotlin.random.Random

class NeuralNetwork (val inputCount:Int, val hiddenCount:Int,  val outputCount:Int):Update{
    private val weightsInputHidden=Matrix(hiddenCount,inputCount)
    private val weightsHiddenOutput=Matrix(outputCount,hiddenCount)
    private val biasHidden=Matrix(hiddenCount,1)
    private val biasOutput=Matrix(outputCount,1)

    private val inputLayer= List(inputCount,init = {Circle(0f,0f,10f)})
    private val hiddenLayer=List(hiddenCount,init = {Circle(0f,0f,10f)})
    private val outputLayer=List(outputCount,init = {Circle(0f,0f,10f)})

    constructor(network: NeuralNetwork)
            :this(network.inputCount,network.hiddenCount, network.outputCount){
        weightsHiddenOutput.copy(network.weightsHiddenOutput)
        weightsInputHidden.copy(network.weightsInputHidden)
        biasHidden.copy(network.biasHidden)
        biasOutput.copy(network.biasOutput)
    }


    companion object {
        fun mutate(child: NeuralNetwork,rate:Float) {
            child.weightsInputHidden.mutate(rate)
            child.weightsHiddenOutput.mutate(rate)
            child.biasHidden.mutate(rate)
            child.biasOutput.mutate(rate)
        }
    }

    fun breed():NeuralNetwork{
        //create a child with parents genes
        return NeuralNetwork(this)
    }


    //forward propagation
    fun predict(values:MutableList<Double>):MutableList<Double>{
        val input=Matrix.fromArray(values)
        val hidden=Matrix.multiply(weightsInputHidden,input)
        hidden.add(biasHidden)
        hidden.sigmoid()

        val output=Matrix.multiply(weightsHiddenOutput,hidden)
        output.add(biasOutput)
        output.sigmoid()
        return output.toArray()
    }

    override fun draw(batch: Batch) {

        inputLayer.forEach {
            batch.draw(it)
        }
        hiddenLayer.forEach {
            batch.draw(it)
        }
        outputLayer.forEach {
            batch.draw(it)
        }
    }

    override fun update(delta: Long) {

    }
}