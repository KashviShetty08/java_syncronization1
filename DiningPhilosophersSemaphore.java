
import java.util.*;
import java.nio.file.*;
import java.awt.Desktop;
import java.util.concurrent.Semaphore;

// Dining Philosophers using semaphores
public class DiningPhilosophersSemaphore {

    static final int N = 4;

    static Semaphore[] forks = new Semaphore[N];
    static Semaphore room = new Semaphore(N - 1);

    static List<String> events = new ArrayList<>();
    static boolean[] eating = new boolean[N];
    static long startTime;

    // Record each philosopher's state for the HTML replay
    static synchronized void log(int id, String action, String message) {
        double time = (System.nanoTime() - startTime) / 1e9;

        StringBuilder states = new StringBuilder();
        for (int i = 0; i < N; i++) {
            states.append(eating[i] ? "Eating" : "Thinking");
            if (i < N - 1) states.append(",");
        }

        events.add(String.format(Locale.US,
            "{\"time\":%.3f,\"id\":%d,\"action\":\"%s\","
            + "\"message\":\"%s\",\"states\":\"%s\"}",
            time, id, action, message, states));
        System.out.println(message);
    }

    static class Philosopher extends Thread {
        int id;

        Philosopher(int id) {
            this.id = id;
        }

        public void run() {
            int left = id;
            int right = (id + 1) % N;

            try {
                log(id, "Thinking", "Philosopher " + (id + 1) + " is thinking.");
                Thread.sleep(400);

                // Limit the number of philosophers competing for forks
                room.acquire();

                forks[left].acquire();
                log(id, "Waiting",
                    "Philosopher " + (id + 1) + " picked up the left fork.");

                forks[right].acquire();

                synchronized (eating) {
                    eating[id] = true;
                }
                log(id, "Eating",
                    "Philosopher " + (id + 1) + " is eating.");

                Thread.sleep(700);

                synchronized (eating) {
                    eating[id] = false;
                }

                forks[right].release();
                forks[left].release();
                room.release();

                log(id, "Thinking",
                    "Philosopher " + (id + 1) + " put down both forks.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        startTime = System.nanoTime();

        // Create one semaphore for each fork
        for (int i = 0; i < N; i++) {
            forks[i] = new Semaphore(1);
        }

        Thread[] philosophers = new Thread[N];

        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i);
            philosophers[i].start();
        }

        // Wait for all philosophers to finish
        for (Thread p : philosophers) {
            p.join();
        }

        generateHTML();
    }

    // Create an HTML page that replays the recorded events
    static void generateHTML() throws Exception {
        String data = "[" + String.join(",", events) + "]";

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            <title>Dining Philosophers - Semaphores</title>
            <style>
            body{font-family:Arial;background:#edf1f5;color:#18324b;padding:20px}
            .card{background:white;padding:20px;margin:20px auto;max-width:900px;border-radius:15px}
            button{padding:10px 16px;margin:4px;background:#216ca8;color:white;border:0;cursor:pointer}
            #table{width:240px;height:240px;border-radius:50%;background:#f7e7c9;border:5px solid #c9a86b;position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center;font-weight:bold}
            #scene{height:390px;position:relative}
            .p{position:absolute;width:90px;padding:15px 0;text-align:center;border-radius:18px;background:#5795d5;color:white;z-index:2;transition:.3s}
            .Eating{background:#38a56a!important}
            .Waiting{background:#e5a13b!important}
            #p0{left:42%;top:0} #p1{right:3%;top:38%}
            #p2{left:42%;bottom:0} #p3{left:3%;top:38%}
            #barbg{height:10px;background:#ddd}#bar{height:10px;background:#216ca8;width:0}
            li{margin:8px 0}
            </style>
            </head>
            <body>
            <div class="card">
            <h1>Dining Philosophers Problem</h1>
            <p>Semaphore-Based Synchronization</p>
            <div id="scene">
              <div id="table">Dining Table</div>
              <div class="p" id="p0">P1<br>Thinking</div>
              <div class="p" id="p1">P2<br>Thinking</div>
              <div class="p" id="p2">P3<br>Thinking</div>
              <div class="p" id="p3">P4<br>Thinking</div>
            </div>
            <h3 id="message">Press Play to start</h3>
            <button onclick="play()">Play</button><button onclick="pause()">Pause</button>
            <button onclick="next()">Next Event</button><button onclick="reset()">Reset</button>
            <div id="barbg"><div id="bar"></div></div>
            </div>
            <div class="card"><h2>Execution Events</h2><ol id="logs"></ol></div>
            <script>
            const events=EVENT_DATA;let i=0,timer=null;
            function show(){
              if(i>=events.length){pause();return}
              const e=events[i++];
              document.getElementById("message").textContent=e.message;
              const p=document.getElementById("p"+e.id);
              p.className="p "+e.action;
              p.innerHTML="P"+(e.id+1)+"<br>"+e.action;
              const li=document.createElement("li");li.textContent=e.time+"s — "+e.message;
              document.getElementById("logs").appendChild(li);
              document.getElementById("bar").style.width=(i/events.length*100)+"%";
            }
            function play(){if(timer===null&&i<events.length)timer=setInterval(show,800)}
            function pause(){clearInterval(timer);timer=null}
            function next(){pause();show()}
            function reset(){pause();i=0;document.getElementById("logs").innerHTML="";
              document.getElementById("message").textContent="Press Play to start";
              document.getElementById("bar").style.width="0%";
              for(let n=0;n<4;n++){let p=document.getElementById("p"+n);p.className="p";p.innerHTML="P"+(n+1)+"<br>Thinking"}}
            </script>
            </body>
            </html>
            """.replace("EVENT_DATA", data);

        Path file = Path.of("dining_philosophers_semaphore.html");
        Files.writeString(file, html);
        System.out.println("HTML created: " + file.toAbsolutePath());

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(file.toUri());
        }
    }
}
