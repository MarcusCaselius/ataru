# lomake-editori

## Development Mode

### Compile css:

Compile css file once.

```
lein less once
```

Automatically recompile css file on change.

```
lein less auto
```

### Create database

Install PostgreSQL

Init Postgres database files to a desired location (this will create a subdir here)

```
initdb -d atarudb
```

Run the server
 
```
postgres -D atarudb
```

In another shell, create the DB:

```
createdb ataru
```

Create user for the db:

```
createuser -s oph -P
```

Give it a password `oph`

### Run application:

This will also allow you to connect to the nREPL servers of the jvm processes individually and change running code without restarting the JVM.

```
lein virkailija-dev
(in another terminal)
lein figwheel dev
```
Figwheel will automatically push cljs changes to the browser.

Browse to [http://localhost:8350](http://localhost:8350).

### Browser tests:

Browser tests can be run by invoking

```
npm install

# In one terminal window, run the actual application, for example:
lein virkailija-dev

# In second window, you'll want to run figwheel in dev profile as normally:
lein figwheel dev

# And in the third window, run cljsbuild for tests to automatically recompile test.js
lein cljsbuild auto test
```

After this you can run tests by navigating to http://localhost:8350/lomake-editori/test.html .

### Backend tests

To run tests once:

```
lein spec
```

To run them automatically whenever code changes, use `-a`.

### Clojure linter

```
lein eastwood
```

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn). 

## Production Build

```
lein clean
lein cljsbuild once min
```
