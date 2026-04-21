# JMeter load test

Test plan: `async-and-race-load-test.jmx`

The plan covers these scenarios:

- `All entity API workflow`: creates and exercises Users, Categories, Exercises,
  Workouts, and Workout Sets through POST, GET, PUT, and search/bulk endpoints.
  DELETE requests are intentionally excluded.
- `Async task workflow`: each virtual user starts
  `POST /api/v1/async-tasks/workout-set-statistics`, extracts `taskId`, then checks
  `GET /api/v1/async-tasks/{taskId}`.
- `Race condition demo load`: requests
  `GET /api/v1/race-condition/demo?threads=64&incrementsPerThread=10000`.

Run command:

```bash
jmeter -n -t async-and-race-load-test.jmx -l target/jmeter/full-api-results.jtl -e -o target/jmeter/report
```

Environment note:

- Maven verification was executed successfully on 2026-04-21 with Java 17:
  `./mvnw.cmd -q clean verify`.
- Apache JMeter is not installed in this workspace, so the `.jtl` result file and HTML
  report could not be generated here.
- After installing JMeter and starting the app on `localhost:8080`, run the command
  above and use the generated `target/jmeter/report/index.html` as the final report.

Result table template:

| Scenario | Samples | Error % | Average | P95 | Throughput |
| --- | ---: | ---: | ---: | ---: | ---: |
| All entity API workflow | Fill from JMeter Summary Report | Fill | Fill | Fill | Fill |
| Async task workflow | Fill from JMeter Summary Report | Fill | Fill | Fill | Fill |
| Race condition demo load | Fill from JMeter Summary Report | Fill | Fill | Fill | Fill |
