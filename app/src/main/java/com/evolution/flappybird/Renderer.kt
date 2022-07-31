package com.evolution.flappybird

import android.content.Context
import android.opengl.GLES32
import com.evolution.flappybird.ai.NeuralNetwork
import com.evolution.flappybird.entities.Bird
import com.evolution.flappybird.entities.Pillars
import com.graphics.glcanvas.engine.Batch
import com.graphics.glcanvas.engine.Camera2D
import com.graphics.glcanvas.engine.GLRendererView
import com.graphics.glcanvas.engine.maths.AxisABB
import com.graphics.glcanvas.engine.maths.ColorRGBA
import com.graphics.glcanvas.engine.maths.Vector2f
import com.graphics.glcanvas.engine.structures.Font
import com.graphics.glcanvas.engine.ui.*
import com.graphics.glcanvas.engine.utils.TextureLoader
import kotlin.math.min
import kotlin.random.Random

class Renderer(private val context: Context,private var width:Float,private var height:Float):GLRendererView(width,height) {

    private val pillars=Pillars(Vector2f(100f,0f), Vector2f(15000f,height),300f,150f,150f,height)
    private val camera=Camera2D(10f)
    private val uICamera=Camera2D(10f)
    private val batch=Batch()
    private val birds= MutableList(300,init = {Bird(100f,height*0.5f,50f,50f,pillars.maxDistance.x,height)})
    private val deadBirds= mutableListOf<Bird>()
    private val axis=AxisABB()
    private val layout=RelativeLayoutConstraint(null,width, height)
    private val font=Font("fonts/harrington.fnt",context)
    private val generationLabel=GLLabel(250f,80f,font,"Generation: 1",0.3f)
    private val birdLabel=GLLabel(300f,80f,font,"Birds Alive: 1",0.3f)
    private val slider=GLProgressBar(200f,30f,1f,true)
    private var generationCounter=0
    override fun prepare() {
        TextureLoader.getInstance().getTexture(context,"fonts/harrington.png")
        batch.initShader(context)
        camera.setOrtho(width, height)
        uICamera.setOrtho(width, height)

        layout.set(width*0.5f,height*0.5f)
        layout.setBackgroundColor(ColorRGBA.transparent)
        slider.setForegroundColor(ColorRGBA.red)
        slider.getConstraints().layoutMarginLeft(10f)
        slider.getConstraints().layoutMarginTop(20f)

        //details section
        val detailsLayout=LinearLayoutConstraint(layout,450f,200f)
            detailsLayout.setOrientation(LinearLayoutConstraint.VERTICAL)
            detailsLayout.addItem(generationLabel)
            detailsLayout.addItem(birdLabel)
            detailsLayout.addItem(slider)
            detailsLayout.setColor(ColorRGBA.cyan)
            detailsLayout.roundedCorner(10f)
            layout.addItem(detailsLayout)
            detailsLayout.getConstraints().alignTopRight(layout)
            generationLabel.getConstraints().layoutMarginTop(10f)
            generationLabel.setTextColor(ColorRGBA.red)
            generationLabel.setCenterText(false)
            birdLabel.setTextColor(ColorRGBA.red)
            birdLabel.setCenterText(false)
            layout.setZ(1f)


        slider.setOnClickListener(object :OnClickEvent.OnClickListener{
            override fun onClick() {

            }
        })

        getController()?.addEvent(slider)


    }




    override fun draw() {
        GLES32.glClear(GLES32.GL_DEPTH_BUFFER_BIT or GLES32.GL_COLOR_BUFFER_BIT)
        GLES32.glClearColor(0.6f,0.6f,0.6f,1f)

        batch.begin(camera)
        pillars.draw(batch)

        for(i in 0 until birds.size){
           val  bird=birds[i]
                bird.draw(batch)
          }

        batch.end()

        //draw UI elements
        batch.begin(uICamera)
        layout.draw(batch)
        batch.end()

    }


    private fun rand(seed:Int):Int{
        return Random.nextInt(seed)
    }

    private fun createBird(network: NeuralNetwork):Bird{
       return  Bird(100f,height*0.5f,50f,50f,pillars.maxDistance.x,height,network)
    }

    private fun geneticsAlgorithm(){
        val cycles=slider.getProgress()*10/100f+1

        for(q in 0 until (cycles).toInt()) {
            birds.sortBy { it.score }


            // pick the closest pillar
            var closest = Double.MAX_VALUE
            var closestObj = pillars.bars[0]


            //pick the closest pillar
            for (i in 0 until birds.size) {
                val bird = birds[i]
                pillars.bars.forEach { pillar ->
                    val value1 = pillars.closest(bird, pillar.second)
                    val value2 = pillars.closest(bird, pillar.first)
                    //calculate the closest pillar for every bird
                    if (value1 < closest && value1 <= value2 && bird.getX() < (pillar.second.getX()+pillar.second.getWidth()*0.5f)) {
                        closest = value1
                        closestObj = pillar
                    }

                }

                bird.update(closestObj)
                if (!bird.alive) {
                    deadBirds.add(bird)

                }
            }

            //test if any bird has arrived to the end and reset
            for (i in 0 until birds.size) {
                val pillar=pillars.bars[pillars.bars.size-1]
                //calculate the closest pillar for every bird
                if ((birds[i].getX()-200f) >(pillar.second.getX()+pillar.second.getWidth()*0.5f)) {
                   birds.forEach { it.alive=false }
                    pillars.reset()
                    break
                }
            }


            birds.removeAll { !it.alive }

            //test collision between pillars and birds
            birds.forEach { bird ->
                for (i in 0 until pillars.bars.size) {
                    if (axis.isIntersecting(bird, pillars.bars[i].first) ||
                        axis.isIntersecting(bird, pillars.bars[i].second)
                    ) {
                        bird.alive = false
                        deadBirds.add(bird)
                        break
                    }
                }
            }

            birds.removeAll { !it.alive }

            if (birds.size > 0) {
                // move the camera based on the best bird
                camera.setPosition2D(birds[0].getX() - 200.0f, camera.getEye().y)
            } else {
                generationCounter++
                // pillars.reset()
                deadBirds.sortBy { it.score }
                for (i in 0 until deadBirds.size) {
                    val bird = deadBirds[i]
                    bird.reset()
                    bird.alive = true
                    birds.add(bird)
                }
                deadBirds.clear()

                val new_pop = mutableListOf<Bird>()
                //mate the birds and create a new population
                for (i in 0 until birds.size / 2) {
                    //pick a random parents in population
                        //only the strong will breed hahahaha beta males won't get nothing
                    val parent1 = birds[min(birds.size / 2 + rand(birds.size / 2), birds.size - 1)]
                    //create a child
                    val child = parent1.network.breed()
                    //50% mutation rate, this creates variation
                    NeuralNetwork.mutate(child, 0.5f)
                    val bird = createBird(child)
                    new_pop.add(bird)

                }

                //replace previously weak  generation
                for (i in 0 until new_pop.size) {
                    birds[i] = new_pop[i]

                }
            }

        }

    }
    override fun update(delta: Long) {

        geneticsAlgorithm()
        birdLabel.setText("Birds Alive: "+birds.size)
        generationLabel.setText("Generation: $generationCounter")

    }
}