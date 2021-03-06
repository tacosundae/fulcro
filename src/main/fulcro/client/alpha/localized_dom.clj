(ns fulcro.client.alpha.localized-dom
  (:refer-clojure :exclude [map meta time])
  (:require
    [clojure.future :refer :all]
    [fulcro.util :as util]
    [fulcro.client.alpha.dom :as adom]
    fulcro.client.alpha.dom-common
    fulcro.client.alpha.localized-dom-common)
  (:import (cljs.tagged_literals JSValue)))

(defn emit-tag [str-tag-name args]
  (let [conformed-args (util/conform! ::adom/dom-macro-args args)
        {attrs    :attrs
         children :children
         css      :css} conformed-args
        css-props      (if css `(fulcro.client.alpha.localized-dom-common/add-kwprops-to-props nil ~css) nil)
        children       (mapv second children)
        attrs-type     (or (first attrs) :nil)              ; attrs omitted == nil
        attrs-value    (or (second attrs) {})]
    (case attrs-type
      :js-object
      (let [attr-expr `(fulcro.client.alpha.localized-dom-common/add-kwprops-to-props ~attrs-value ~css)]
        `(fulcro.client.alpha.dom/macro-create-element*
           ~(JSValue. (into [str-tag-name attr-expr] children))))

      :map
      (let [attr-expr (if (or css (contains? attrs-value :classes))
                        `(fulcro.client.alpha.localized-dom-common/add-kwprops-to-props ~(adom/clj-map->js-object attrs-value) ~css)
                        (adom/clj-map->js-object attrs-value))]
        `(fulcro.client.alpha.dom/macro-create-element* ~(JSValue. (into [str-tag-name attr-expr] children))))

      :runtime-map
      (let [attr-expr `(fulcro.client.alpha.localized-dom-common/add-kwprops-to-props ~(adom/clj-map->js-object attrs-value) ~css)]
        `(fulcro.client.alpha.dom/macro-create-element*
           ~(JSValue. (into [str-tag-name attr-expr] children))))

      :symbol
      `(fulcro.client.alpha.localized-dom/macro-create-element
         ~str-tag-name ~(into [attrs-value] children) ~css)

      ;; also used for MISSING props
      :nil
      `(fulcro.client.alpha.dom/macro-create-element*
         ~(JSValue. (into [str-tag-name css-props] children)))

      ;; pure children
      `(fulcro.client.alpha.localized-dom/macro-create-element
         ~str-tag-name ~(JSValue. (into [attrs-value] children)) ~css))))

(adom/gen-dom-macros fulcro.client.alpha.localized-dom/emit-tag)
