package com.comcast.freeflow.core;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.comcast.freeflow.helpers.DefaultSectionAdapter;
import com.comcast.freeflow.layouts.AbstractLayout;
import com.comcast.freeflow.layouts.VLayout;
import com.comcast.freeflow.teststub.MainActivity;

import android.app.Activity;
import android.graphics.Rect;
import android.test.ActivityInstrumentationTestCase2;
import android.view.ViewTreeObserver.OnPreDrawListener;

public class ContainerTest extends ActivityInstrumentationTestCase2<MainActivity> {
	
	Activity main ;
	
	public ContainerTest() {
		super(MainActivity.class);
		
	}

	protected void setUp() throws Exception {
		super.setUp();
		main = getActivity();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Tests that changing an attached layout cannot change 
	 * the internal map of the item proxies insode a container
	 * 
	 * @throws InterruptedException
	 */
	public void testSourceCannotModifyContainerReferences() throws InterruptedException{
		final CountDownLatch lock = new CountDownLatch(1);
		main.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				final Container container = new Container(main);
							
				
				final VLayout vLayout = new VLayout();
				vLayout.setLayoutParams(new VLayout.LayoutParams(300, 200, 10));
				container.setLayout(vLayout);
				
				final DefaultSectionAdapter adapter = new DefaultSectionAdapter(main, 1, 2);
				container.setAdapter(adapter);
				
				
				main.setContentView(container);	
				
				container.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						
						int frameCount = container.getFrames().size();
						adapter.setData(5, 10);
						
						// setItems will force new frames to be generated, but not set 
						vLayout.setAdapter(adapter); 
						vLayout.generateItemProxies() ;
						
						//assertEquals("Layout frames did not generate as expected", 5*(10+1), vLayout.getAllProxies().size());
						assertEquals("Container frames changed unexpectedly with data", frameCount, container.getFrames().size());
						
						lock.countDown();
						return false;
					}
				});
			}
		});
		lock.await(5000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Tests that all items are generated by the time predraw kicks in
	 * 
	 * @throws InterruptedException
	 */
	public void testAllViewsAreGeneratedByFirstLayout() throws InterruptedException{
		final CountDownLatch lock = new CountDownLatch(1);
		main.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				final Container container = new Container(main);
				
				DefaultSectionAdapter adapter = new DefaultSectionAdapter(main, 1, 2);
				container.setAdapter(adapter);
				
				VLayout vLayout = new VLayout();
				vLayout.setLayoutParams(new VLayout.LayoutParams(300, 200, 10));
				container.setLayout(vLayout);
				
				container.addFreeFlowEventListener( new FreeFlowEventListener() {
					
					@Override
					public void onLayoutChanging(AbstractLayout oldLayout,
							AbstractLayout newLayout) {
					}
					
					@Override
					public void layoutComputed() {
					}
					
					@Override
					public void layoutComplete(boolean areTransitionAnimationsPlaying) {
						assertEquals("Correct number of children were not created", 3, container.getChildCount());
					}
					
					@Override
					public void dataChanged() {
					}
					
					@Override
					public void animationsStarted() {
					}
					
					@Override
					public void animationsComplete() {
					}
				} );
				
				main.setContentView(container);	
				
				
			}
		});
		lock.await(5000, TimeUnit.MILLISECONDS);
		
	}
	
	/**
	 * Tests if a view is within the viewport and is moved,
	 */
	public void testViewChangesWhenViewMovesWithinViewport(){
		
		HashMap<String, ItemProxy> oldMap = new HashMap<String, ItemProxy>();
		HashMap<String, ItemProxy> newMap = new HashMap<String, ItemProxy>();
		
		
		String one = "one";
		ItemProxy proxy = new ItemProxy();
		proxy.data = one;
		proxy.frame = new Rect(0,0, 20, 20);
		oldMap.put(one, proxy);
		
		ItemProxy proxy2 = new ItemProxy();
		proxy.data = one;
		proxy.frame =  new Rect(20,20,20+40,20+40);
		newMap.put(one, proxy2);
		
		Container container = new Container(getActivity());
		LayoutChangeSet changes = container.getViewChanges(oldMap, newMap);
		
		assertTrue(changes.getMoved().size() == 1);
		assertTrue(changes.getAdded().size() == 0);
		assertTrue(changes.getRemoved().size() == 0);
	}
	
	
//	public void testAsync(){
//		Container c = new Container(getActivity());
//		c.setLayoutParams(new LayoutParams(400,400));
//		
//	}
	
	
}