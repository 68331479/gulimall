package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @Author: LDeng
 * @Date: 2020-12-24 14:19
 */
public class ThreadTest {

    /*
        继承Thread
        实现Runnable接口
        实现Callable接口+FutureTask(可以拿到返回结果,可以处理异常)
        线程池, 给线程池提交任务
     */

    public static ExecutorService service = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main() >>>>>>>>>>>>start");
        //1, CompletableFuture.runAsync
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, service);

        // 2,CompletableFuture
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service)
         //方法成功完成后的处理
//                .whenComplete((res, exception) -> {//感知异常
//                    System.out.println("异步任务成功完成！！！！结果是:" + res + ";   异常是：" + exception);
//                })
//                .exceptionally(throwable -> {
//                    return 10;//指定出现异常后的返回值；
//                });
//        Integer integer = future.get();
         //方法执行完成后的处理
         //handle(BiFunction<? super T, Throwable, ? extends U> fn)
//        .handle((res,exception)->{
//            if(res!=null){
//                return res*2;
//            }
//            if(exception!=null){
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = future.get();

        //thenRunAsync---不能获取到上一步的执行结果
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenRunAsync(() -> {
//            System.out.println("任务2启动了");
//        }, service);

        //thenAccept---接收上一步异步执行的结果, 但是无返回值
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenAcceptAsync(res->{
//            System.out.println("任务2启动了, 接收到的上一步异步任务的结果是："+res);
//        },service);
//
        //thenApply--接收上一步异步执行的结果，并有返回值
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了, 接收到的上一步异步任务的结果是：" + res);
//            return "Hello " + res;
//        }, service);

        //两个任务都完成, 无需前两个任务的结果
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("线程03开始");
//        },service);
//


        //两个任务都完成, 需要前两个任务的结果
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        future01.thenAcceptBothAsync(future02,(f1,f2)->{
//            System.out.println("线程03开始"+"上两步的结果:"+f1+"&&"+f2);
//        },service);


        // 得到1，2的执行结果， 再执行3 ， 并有返回值
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            System.out.println("线程03开始" + "上两步的结果:" + f1 + "&&" + f2);
//            return f1 + "-----" + f2;
//        }, service);
//        String s = future.get();

        //两个任务只要完成一个, 无需前两个任务的结果
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        future01.runAfterEitherAsync(future02,()->{
//            System.out.println("线程03开始");
//        },service);

        //两个任务只要完成其中一个， 将一个作为线程3的入参， 线程3无返回值
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        future01.acceptEitherAsync(future02,(res)->{
//            System.out.println("线程03开始"+"上两步的结果:"+res);
//        },service);

            //两个任务只要完成其中一个， 将一个作为线程3的入参， 并且线程3有返回值
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程01：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("线程01结束。。。。");
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程02：" + Thread.currentThread().getId());
//            String s = "Hello";
//            System.out.println("线程02结束。。。。");
//            return s;
//        }, service);
//
//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, (res) -> {
//            System.out.println("线程03开始" + "上两步的结果:" + res);
//            return "这是线程3返回的结果:" + res;
//        }, service);
//        String s = future.get();

//        //多个异步任务全部执行完成
//        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
//            System.out.println("查询商品的图片信息");
//            return "图片.jpg";
//        },service);
//        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
//            System.out.println("查询商品的属性信息");
//            return "属性.jpg";
//        },service);
//        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
//            try {
//                Thread.sleep(3000);
//                System.out.println("查询商品的介绍");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return "介绍.jpg";
//        },service);
//
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        allOf.get();//等待所有结果完成
//
//        System.out.println("main() <<<<<<end<<<<<"+futureImg.get()+"------"+futureAttr.get()+"------"+futureDesc.get());

        //多个异步任务只要有一个执行完成
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品的图片信息");
            return "图片.jpg";
        },service);
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(6000);
                System.out.println("查询商品的属性信息");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "属性.jpg";
        },service);
        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("查询商品的介绍");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "介绍.jpg";
        },service);

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);

        System.out.println("main() <<<<<<end<<<<<"+anyOf.get());

    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main() >>>>>>>>>>>>start");
        //1  继承Thread类
//        Thread01 thread01 = new Thread01();
//        thread01.start();
        //2  实现Runnable接口
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();
        //3 实现Callable接口
//        Callable01 callable01 = new Callable01();
//        FutureTask<Integer> futureTask = new FutureTask<>(callable01);
//        //等待线程执行完成获取返回结果
//        new Thread(futureTask).start();
//        Integer integer = futureTask.get();
//        System.out.println("main() <<<<<<end"+integer);

        //4，使用线程池 给线程池提交任务  实际业务中使用线程池， 避免资源浪费
        //将所有的多线程异步任务都交给线程池执行
        //并且在整个系统统一创建一个线程池， 每个异步任务直接提交给线程池

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        Executors.newCachedThreadPool();
        Executors.newFixedThreadPool(10);//核心
        Executors.newScheduledThreadPool(10);//做定时任务的线程池
        Executors.newSingleThreadExecutor();//单线程的线程池，一个一个任务执行

        service.execute(new Runnable01());

    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果：" + i);
            return i;
        }
    }


}
