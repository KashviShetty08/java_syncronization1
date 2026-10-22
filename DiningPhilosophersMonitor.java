import java.awt.Desktop;
import java.nio.file.*;
import java.util.*;

// Dining Philosophers using Java monitors
public class DiningPhilosophersMonitor {

    static final int N = 4;

    static boolean[] eating = new boolean[N];
    static boolean[] forkInUse = new boolean[N];
    static String[] states = {
        "Thinking", "Thinking", "Thinking", "Thinking"
    };

    static List<String> events =
        Collections.synchronizedList(new ArrayList<>());

    static long startTime;

    static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    static String statesJSON() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < N; i++) {
            if (i > 0) result.append(",");
            result.append("\"").append(states[i]).append("\"");
        }
        return result.append("]").toString();
    }

    static String forksJSON() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < N; i++) {
            if (i > 0) result.append(",");
            result.append(forkInUse[i]);
        }
        return result.append("]").toString();
    }

    // Record each event for the HTML animation
    static void log(int id, String action, String message) {
        synchronized (events) {
            double time = (System.nanoTime() - startTime) / 1e9;

            events.add(String.format(Locale.US,
                "{\"time\":%.3f,\"id\":%d,\"philosopher\":\"P%d\","
                + "\"action\":\"%s\",\"message\":\"%s\","
                + "\"states\":%s,\"forks\":%s}",
                time, id, id + 1, escape(action), escape(message),
                statesJSON(), forksJSON()));

            System.out.println(message);
        }
    }

    // Monitor controls access to chopsticks
    static class Table {

        synchronized void takeForks(int id)
                throws InterruptedException {

            int left = (id + N - 1) % N;
            int right = (id + 1) % N;

            states[id] = "Waiting";
            log(id, "Waiting",
                "Philosopher " + (id + 1)
                + " is waiting for chopsticks.");

            // Wait until both neighboring philosophers are not eating
            while (eating[left] || eating[right]) {
                wait();
            }

            eating[id] = true;
            states[id] = "Eating";

            // This philosopher picks up the two adjacent chopsticks
            forkInUse[left] = true;
            forkInUse[id] = true;

            log(id, "Eating",
                "Philosopher " + (id + 1)
                + " picks up both chopsticks and starts eating.");
        }

        synchronized void putForks(int id) {
            int left = (id + N - 1) % N;

            eating[id] = false;
            states[id] = "Thinking";

            forkInUse[left] = false;
            forkInUse[id] = false;

            log(id, "Released",
                "Philosopher " + (id + 1)
                + " puts down both chopsticks.");

            notifyAll();
        }
    }

    static class Philosopher extends Thread {
        int id;
        Table table;

        Philosopher(int id, Table table) {
            this.id = id;
            this.table = table;
        }

        public void run() {
            try {
                for (int round = 0; round < 2; round++) {
                    synchronized (table) {
                        states[id] = "Thinking";
                        log(id, "Thinking",
                            "Philosopher " + (id + 1)
                            + " is thinking.");
                    }

                    Thread.sleep(500);

                    table.takeForks(id);

                    Thread.sleep(700);

                    table.putForks(id);
                }

                synchronized (table) {
                    states[id] = "Finished";
                    log(id, "Finished",
                        "Philosopher " + (id + 1) + " finished.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                synchronized (table) {
                    states[id] = "Thinking";
                    log(id, "Interrupted",
                        "Philosopher " + (id + 1) + " was interrupted.");
                    table.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        startTime = System.nanoTime();

        Table table = new Table();
        Thread[] philosophers = new Thread[N];

        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i, table);
            philosophers[i].start();
        }

        for (Thread p : philosophers) {
            p.join();
        }

        generateHTML();
    }

    // Create the HTML simulation after the Java threads finish
    static void generateHTML() throws Exception {
        String data;

        synchronized (events) {
            data = String.join(",", events);
        }

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>Dining Philosophers - Monitor</title>
            <style>
            body{font-family:Arial;background:#edf4fb;color:#12366d;padding:18px}
            .card{background:white;border:1px solid #d9e7f8;border-radius:14px;padding:18px;margin:15px auto;max-width:1050px}
            h1{text-align:center}
            button{padding:10px 16px;margin:4px;background:#0877c9;color:white;border:0;border-radius:6px;cursor:pointer}
            .layout{display:grid;grid-template-columns:2fr 1fr;gap:16px}
            .scene{height:420px;position:relative;background:#fbfdff;overflow:hidden}
            .table{width:220px;height:220px;border-radius:50%;background:#f8dfba;border:7px solid #bd8747;position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center}
            .p{position:absolute;width:82px;height:68px;text-align:center;border-radius:50%;background:#1688e7;border:3px solid #104b83;color:white;z-index:2;display:flex;flex-direction:column;align-items:center;justify-content:center}
            .p small{font-size:12px;margin-top:3px}
            .Eating{background:#20a987!important}
            .Waiting{background:#f2a329!important}
            .Finished{background:#718096!important}
            #p0{left:calc(50% - 41px);top:3px}
            #p1{right:3%;top:calc(50% - 39px)}
            #p2{left:calc(50% - 41px);bottom:3px}
            #p3{left:3%;top:calc(50% - 39px)}
            .fork{position:absolute;width:12px;height:42px;background:#b78350;border:2px solid #704a2a;border-radius:3px;z-index:3}
            .fork.used{background:#e02020;border-color:#8d1111}
            #f0{left:calc(50% - 68px);top:calc(50% - 144px);transform:rotate(42deg)}
            #f1{left:calc(50% + 48px);top:calc(50% - 144px);transform:rotate(-42deg)}
            #f2{left:calc(50% + 116px);top:calc(50% - 20px)}
            #f3{left:calc(50% - 132px);top:calc(50% - 20px)}
            .key{display:inline-block;width:30px;height:8px;background:#b78350;margin:0 8px}
            .red{background:#e02020}
            .barbg{height:10px;background:#ddd;border-radius:8px;overflow:hidden}
            #bar{height:100%;width:0;background:#0877c9}
            .meta{display:flex;justify-content:space-between;flex-wrap:wrap;margin-top:10px}
            table{width:100%;border-collapse:collapse}
            td,th{text-align:left;padding:9px;border-bottom:1px solid #ddd}
            th{background:#edf4ff}
            @media(max-width:700px){.layout{grid-template-columns:1fr}}
            </style>
            </head>
            <body>
            <div class="card">
            <h1>Dining Philosophers Problem</h1>
            <p style="text-align:center">Monitor-Based Synchronization</p>
            <button onclick="play()">Play</button>
            <button onclick="pause()">Pause</button>
            <button onclick="next()">Next Event</button>
            <button onclick="reset()">Reset</button>
            <div class="barbg"><div id="bar"></div></div>
            <div class="meta">
            <span id="count">Event: 0</span>
            <span id="time">Time: 0.00 s</span>
            <span id="status">Ready to start</span>
            </div>
            </div>

            <div class="card layout">
            <div>
            <h2>Table Visualization</h2>
            <div class="scene">
            <div class="table">Dining Table</div>
            <div class="fork" id="f0"></div>
            <div class="fork" id="f1"></div>
            <div class="fork" id="f2"></div>
            <div class="fork" id="f3"></div>
            <div class="p" id="p0">P1<small>Thinking</small></div>
            <div class="p" id="p1">P2<small>Thinking</small></div>
            <div class="p" id="p2">P3<small>Thinking</small></div>
            <div class="p" id="p3">P4<small>Thinking</small></div>
            </div>
            </div>

            <div>
            <h3>Philosopher States</h3>
            <p>🔵 Thinking</p><p>🟢 Eating</p><p>🟠 Waiting</p>
            <hr><h3>Chopsticks</h3>
            <p><span class="key"></span>Available</p>
            <p><span class="key red"></span>In use</p>
            <hr><h3>Active Philosophers</h3>
            <div id="active">Eating: None<br>Waiting: None<br>Thinking: P1, P2, P3, P4</div>
            </div>
            </div>

            <div class="card">
            <h2>Execution Event Log</h2>
            <table>
            <thead><tr><th>Time</th><th>Philosopher</th><th>Action</th><th>Message</th></tr></thead>
            <tbody id="rows"><tr><td colspan="4">Press Play to start.</td></tr></tbody>
            </table>
            </div>

            <script>
            const events=[EVENT_DATA];
            let i=0,timer=null;

            function show(){
                if(i>=events.length){pause();return}
                const e=events[i++];
                document.getElementById("count").textContent="Event: "+i+" / "+events.length;
                document.getElementById("time").textContent="Time: "+e.time.toFixed(2)+" s";
                document.getElementById("status").textContent=e.action;
                document.getElementById("bar").style.width=(i/events.length*100)+"%";

                e.states.forEach((s,j)=>{
                    const p=document.getElementById("p"+j);
                    p.className="p"+(s==="Thinking"?"":" "+s);
                    p.innerHTML="P"+(j+1)+"<small>"+s+"</small>";
                });

                e.forks.forEach((used,j)=>{
                    document.getElementById("f"+j).classList.toggle("used",used);
                });

                document.getElementById("active").innerHTML=
                    ["Eating","Waiting","Thinking"].map(s=>
                    s+": "+(e.states.map((v,j)=>v===s?"P"+(j+1):"")
                    .filter(Boolean).join(", ")||"None")).join("<br>");

                const row=document.createElement("tr");
                [e.time.toFixed(2),e.philosopher,e.action,e.message].forEach(v=>{
                    const cell=document.createElement("td");
                    cell.textContent=v;
                    row.appendChild(cell);
                });
                document.getElementById("rows").appendChild(row);
            }

            function play(){
                if(timer===null && i<events.length)
                    timer=setInterval(()=>{
                        if(i<events.length)show();
                        else{pause();document.getElementById("status").textContent="Complete";}
                    },700);
            }
            function pause(){if(timer!==null){clearInterval(timer);timer=null;}}
            function next(){pause();show();}
            function reset(){
                pause();i=0;
                document.getElementById("count").textContent="Event: 0 / "+events.length;
                document.getElementById("time").textContent="Time: 0.00 s";
                document.getElementById("status").textContent="Ready to start";
                document.getElementById("bar").style.width="0";
                document.getElementById("rows").innerHTML='<tr><td colspan="4">Press Play to start.</td></tr>';
                for(let j=0;j<4;j++){
                    let p=document.getElementById("p"+j);
                    p.className="p";p.innerHTML="P"+(j+1)+"<small>Thinking</small>";
                    document.getElementById("f"+j).classList.remove("used");
                }
                document.getElementById("active").innerHTML="Eating: None<br>Waiting: None<br>Thinking: P1, P2, P3, P4";
            }
            </script>
            </body>
            </html>
            """.replace("EVENT_DATA", data);

        Path file = Path.of("dining_philosophers_monitor.html");
        Files.writeString(file, html);
        System.out.println("HTML created: " + file.toAbsolutePath());

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(file.toUri());
        }
    }
}