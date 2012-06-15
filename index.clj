{:namespaces
 ({:source-url nil,
   :wiki-url "caribou.app.controller-api.html",
   :name "caribou.app.controller",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.halo-api.html",
   :name "caribou.app.halo",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.handler-api.html",
   :name "caribou.app.handler",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.i18n-api.html",
   :name "caribou.app.i18n",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.middleware-api.html",
   :name "caribou.app.middleware",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.pages-api.html",
   :name "caribou.app.pages",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.request-api.html",
   :name "caribou.app.request",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.routing-api.html",
   :name "caribou.app.routing",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.template-api.html",
   :name "caribou.app.template",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.util-api.html",
   :name "caribou.app.util",
   :doc nil}
  {:source-url nil,
   :wiki-url "caribou.app.view-api.html",
   :name "caribou.app.view",
   :doc nil}),
 :vars
 ({:arglists ([request key]),
   :name "cookie",
   :namespace "caribou.app.controller",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.controller-api.html#caribou.app.controller/cookie",
   :doc "Get the value from the given cookie.",
   :var-type "function",
   :line 40,
   :file "src/caribou/app/controller.clj"}
  {:arglists ([controller-ns controller-key action-key]),
   :name "get-controller-action",
   :namespace "caribou.app.controller",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.controller-api.html#caribou.app.controller/get-controller-action",
   :doc
   "Find the function corresponding to the given controller namespace and\nits name in that namespace",
   :var-type "function",
   :line 5,
   :file "src/caribou/app/controller.clj"}
  {:arglists ([url] [url params]),
   :name "redirect",
   :namespace "caribou.app.controller",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.controller-api.html#caribou.app.controller/redirect",
   :doc
   "Return a response corresponding to a redirect triggered in the user's browser.",
   :var-type "function",
   :line 32,
   :file "src/caribou/app/controller.clj"}
  {:arglists ([content-type params] [params]),
   :name "render",
   :namespace "caribou.app.controller",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.controller-api.html#caribou.app.controller/render",
   :doc
   "Render the template corresponding to this page and return a proper response.",
   :var-type "function",
   :line 21,
   :file "src/caribou/app/controller.clj"}
  {:arglists ([request func]),
   :name "check-key",
   :namespace "caribou.app.halo",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url "/caribou.app.halo-api.html#caribou.app.halo/check-key",
   :doc
   "Wraps Halo requests and inspects the X-Halo-Key request header.",
   :var-type "function",
   :line 16,
   :file "src/caribou/app/halo.clj"}
  {:arglists ([request]),
   :name "reload-halo",
   :namespace "caribou.app.halo",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url "/caribou.app.halo-api.html#caribou.app.halo/reload-halo",
   :doc "reloads the Halo routes in this Caribou app",
   :var-type "function",
   :line 66,
   :file "src/caribou/app/halo.clj"}
  {:arglists ([request]),
   :name "reload-models",
   :namespace "caribou.app.halo",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.halo-api.html#caribou.app.halo/reload-models",
   :doc "reloads the models in this Caribou app",
   :var-type "function",
   :line 59,
   :file "src/caribou/app/halo.clj"}
  {:arglists ([request]),
   :name "reload-pages",
   :namespace "caribou.app.halo",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.halo-api.html#caribou.app.halo/reload-pages",
   :doc "reloads the Page routes in this Caribou app",
   :var-type "function",
   :line 52,
   :file "src/caribou/app/halo.clj"}
  {:arglists ([]),
   :name "_dynamic-handler",
   :namespace "caribou.app.handler",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.handler-api.html#caribou.app.handler/_dynamic-handler",
   :doc
   "calls the dynamic route generation functions and returns a composite handler",
   :var-type "function",
   :line 53,
   :file "src/caribou/app/handler.clj"}
  {:arglists ([]),
   :name "gen-handler",
   :namespace "caribou.app.handler",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.handler-api.html#caribou.app.handler/gen-handler",
   :doc
   "Returns a function that calls our memoized handler on every request",
   :var-type "function",
   :line 65,
   :file "src/caribou/app/handler.clj"}
  {:arglists ([]),
   :name "reset-handler",
   :namespace "caribou.app.handler",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.handler-api.html#caribou.app.handler/reset-handler",
   :doc
   "clears the memoize atom in the metadata for dynamic-handler, which causes it to 'un-memoize'",
   :var-type "function",
   :line 71,
   :file "src/caribou/app/handler.clj"}
  {:arglists ([]),
   :name "current-locale",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/current-locale",
   :doc "Returns the locale in the current thread",
   :var-type "function",
   :line 17,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([]),
   :name "get-default-locale",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/get-default-locale",
   :doc
   "Gets the default locale from the app config.  Falls back to en_US",
   :var-type "function",
   :line 35,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([resource-key options] [resource-key]),
   :name "get-resource",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/get-resource",
   :doc
   "Usage: (get-resource \"key\")\n        (get-resource \"key\" {:locale \"es_ES\"})\n        (get-resource \"key\" {:values [\"replacement value 1\" \"replacement value 2\"]})\nGets a translation from the DB for the given key and optional options map.",
   :var-type "function",
   :line 64,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([]),
   :name "load-resources",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/load-resources",
   :doc "Loads translations and locales from the DB.",
   :var-type "function",
   :line 22,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([locale func & args]),
   :name "locale-override",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/locale-override",
   :doc
   "Forces the locale to the specified value in the wrapped function",
   :var-type "function",
   :line 86,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([f]),
   :name "set-locale-func",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/set-locale-func",
   :doc
   "Use this to set the function that will be used by the i18n\nsystem to determine the locale",
   :var-type "function",
   :line 80,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([]),
   :name "user-locale-func",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :dynamic true,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.i18n-api.html#caribou.app.i18n/user-locale-func",
   :doc
   "The function that will be called whenever a translation is requested.  \nThe app should override this.",
   :var-type "function",
   :line 41,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([handler locale-func]),
   :name "wrap-i18n",
   :namespace "caribou.app.i18n",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url "/caribou.app.i18n-api.html#caribou.app.i18n/wrap-i18n",
   :doc
   "Ring handler wrapper that ensures that the current locale is set\nin the *current-locale* var",
   :var-type "function",
   :line 92,
   :file "src/caribou/app/i18n.clj"}
  {:arglists ([handler]),
   :name "wrap-servlet-path-info",
   :namespace "caribou.app.middleware",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.middleware-api.html#caribou.app.middleware/wrap-servlet-path-info",
   :doc
   "Removes the deployed servlet context from the request URI when running as a war",
   :var-type "function",
   :line 28,
   :file "src/caribou/app/middleware.clj"}
  {:arglists ([] [tree]),
   :name "create-page-routes",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/create-page-routes",
   :doc
   "Invoke pages from the db and generate the routes based on them.",
   :var-type "function",
   :line 89,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([page template controller-key action-key]),
   :name "generate-action",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/generate-action",
   :doc
   "Depending on the application environment, reload controller files (or not).",
   :var-type "function",
   :line 31,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([pages]),
   :name "generate-page-routes",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/generate-page-routes",
   :doc
   "Given a tree of pages construct and return a list of corresponding routes.",
   :var-type "function",
   :line 65,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([tree]),
   :name "invoke-pages",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/invoke-pages",
   :doc "Call up the pages and arrange them into a tree.",
   :var-type "function",
   :line 83,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([page above-path]),
   :name "match-action-to-template",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/match-action-to-template",
   :doc
   "Make a single route for a single page, given its overarching path (above-path)",
   :var-type "function",
   :line 44,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([controller-key action-key]),
   :name "retrieve-controller-action",
   :namespace "caribou.app.pages",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.pages-api.html#caribou.app.pages/retrieve-controller-action",
   :doc
   "Given the controller-key and action-key, return the function that is correspondingly defined by a controller.",
   :var-type "function",
   :line 21,
   :file "src/caribou/app/pages.clj"}
  {:arglists ([]),
   :name "ring-request",
   :namespace "caribou.app.request",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.request-api.html#caribou.app.request/ring-request",
   :doc "Returns back the current ring request map",
   :var-type "function",
   :line 7,
   :file "src/caribou/app/request.clj"}
  {:arglists ([]),
   :name "clear-routes",
   :namespace "caribou.app.routing",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.routing-api.html#caribou.app.routing/clear-routes",
   :doc "Clears the app's routes. Used by Halo to update the routes.",
   :var-type "function",
   :line 33,
   :file "src/caribou/app/routing.clj"}
  {:arglists ([params]),
   :name "default-action",
   :namespace "caribou.app.routing",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/caribou.app.routing-api.html#caribou.app.routing/default-action",
   :doc
   "if a page doesn't have a defined action, we just send the params to the template",
   :var-type "function",
   :line 39,
   :file "src/caribou/app/routing.clj"})}
