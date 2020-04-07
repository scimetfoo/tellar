(ns tellar.core-test
  (:require [clojure.test :refer :all]
            [tellar.core :refer :all]))

(def nested-structure {:name "Abbey Road"
                       :artist [{:name "The Beatles"}]
                       :tracks [{:name "Come Together"
                                 :number 1
                                 :artist [{:name "The Beatles"}]
                                 :songwriter [{:name "John Lennon"}
                                              {:name "Paul McCartney"}]
                                 :producer [{:name "George Martin"}]}
                                {:name "Something"
                                 :number 2
                                 :artist [{:name "The Beatles"}]
                                 :songwriter [{:name "George Harrison"}]}]})
(deftest trace-test
  (testing "should return all the paths leading to the given node"
    (is (= (trace nested-structure :name)
           [[:name]
            [:artist 0 :name]
            [:tracks 0 :name]
            [:tracks 0 :artist 0 :name]
            [:tracks 0 :songwriter 0 :name]
            [:tracks 0 :songwriter 1 :name]
            [:tracks 0 :producer 0 :name]
            [:tracks 1 :name]
            [:tracks 1 :artist 0 :name]
            [:tracks 1 :songwriter 0 :name]]))
    (is (= (trace nested-structure :tracks)
           [[:tracks]]))
    (is (= (trace nested-structure :artist)
           [[:artist] [:tracks 0 :artist] [:tracks 1 :artist]]))
    (is (= (trace nested-structure :number)
           [[:tracks 0 :number] [:tracks 1 :number]]))
    (is (= (trace nested-structure :songwriter)
           [[:tracks 0 :songwriter] [:tracks 1 :songwriter]]))
    (is (= (trace nested-structure :foo)
           []))))

(deftest dissoc-nth-test
  (testing "dissoc the first occurrence of :name"
    (is (= (dissoc-nth nested-structure :name 1)
           {:artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]})))

  (testing "dissoc the third occurrence of :name"
    (is (= (dissoc-nth nested-structure :name 3)
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]})))

  (testing "dissoc the second occurrence of :name"
    (is (= (dissoc-nth nested-structure :name 2)
           {:name   "Abbey Road"
            :artist [{}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]})))

  (testing "dissoc a key that doesn't exist in the map"
    (is (= (dissoc-nth nested-structure :foo 2) nested-structure)))

  (testing "throw an assertion if n is less than 1"
    (is (thrown? AssertionError (assoc-nth nested-structure :name "foo" 0)))))

(deftest dissoc-all-test
  (testing "dissoc all occurrences of :name"
    (is (= (dissoc-all nested-structure :name)
           {:artist [{}]
            :tracks [{:number     1
                      :artist     [{}]
                      :songwriter [{}
                                   {}]
                      :producer   [{}]}
                     {:number     2
                      :artist     [{}]
                      :songwriter [{}]}]})))

  (testing "dissoc a key that doesn't exist in the map"
    (is (= (dissoc-all nested-structure :foo) nested-structure))))

(deftest assoc-nth-test
  (testing "assoc in place of the first occurrence of :name"
    (is (= (assoc-nth nested-structure :name "foo" 1)
           {:name   "foo"
            :artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]})))

  (testing "assoc in place of the fourth occurrence of :name"
    (is (= (assoc-nth nested-structure :name "foo" 4)
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "foo"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]}))

    (testing "assoc in place of the first occurrence of :artist"
      (is (= (assoc-nth nested-structure :artist "Betelgeuse" 1)
             {:name   "Abbey Road"
              :artist "Betelgeuse"
              :tracks [{:name       "Come Together"
                        :number     1
                        :artist     [{:name "The Beatles"}]
                        :songwriter [{:name "John Lennon"}
                                     {:name "Paul McCartney"}]
                        :producer   [{:name "George Martin"}]}
                       {:name       "Something"
                        :number     2
                        :artist     [{:name "The Beatles"}]
                        :songwriter [{:name "George Harrison"}]}]})))

    (testing "assoc in place of a key that doesn't exist"
      (is (= (assoc-nth nested-structure :record-label "the-record-label-for-this-album" 4)
             {:name   "Abbey Road"
              :artist [{:name "The Beatles"}]
              :tracks [{:name       "Come Together"
                        :number     1
                        :artist     [{:name "The Beatles"}]
                        :songwriter [{:name "John Lennon"}
                                     {:name "Paul McCartney"}]
                        :producer   [{:name "George Martin"}]}
                       {:name       "Something"
                        :number     2
                        :artist     [{:name "The Beatles"}]
                        :songwriter [{:name "George Harrison"}]}]}))))

  (testing "throw an assertion if n is less than 1"
    (is (thrown? AssertionError (assoc-nth nested-structure :name "foo" 0)))))

(deftest assoc-all-test
  (testing "assoc in place of all occurrences of :tracks"
    (is (= (assoc-all nested-structure :tracks "foo")
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks "foo"})))

  (testing "assoc in place of all occurrences of :songwriter"
    (is (= (assoc-all nested-structure :songwriter "Beetle")
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter "Beetle"
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter "Beetle"}]})))

  (testing "assoc in place of a key that doesn't exist"
    (is (= (assoc-all nested-structure :record "the-record-label-for-this-album")
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     1
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     2
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]}))))

(deftest update-all-test
(testing "increment all occurrences of :number by 10"
    (is (= (update-all nested-structure :number + 10)
           {:name   "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:name       "Come Together"
                      :number     11
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer   [{:name "George Martin"}]}
                     {:name       "Something"
                      :number     12
                      :artist     [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]})))

  (testing "update the value of occurrences of :name to beetle"
    (is (= (update-all nested-structure :name (constantly "beetle"))
           {:name "beetle"
            :artist [{:name "beetle"}]
            :tracks [{:name "beetle"
                      :number 1
                      :artist [{:name "beetle"}]
                      :songwriter [{:name "beetle"}
                                   {:name "beetle"}]
                      :producer [{:name "beetle"}]}
                     {:name "beetle"
                      :number 2
                      :artist [{:name "beetle"}]
                      :songwriter [{:name "beetle"}]}]})))

  (testing "update the value of occurrences of a key that doesn't exist"
    (is (= (update-all nested-structure :foo (constantly "beetle"))
           {:name "Abbey Road"
            :artist [{:name "The Beatles"}]
            :tracks [{:name "Come Together"
                      :number 1
                      :artist [{:name "The Beatles"}]
                      :songwriter [{:name "John Lennon"}
                                   {:name "Paul McCartney"}]
                      :producer [{:name "George Martin"}]}
                     {:name "Something"
                      :number 2
                      :artist [{:name "The Beatles"}]
                      :songwriter [{:name "George Harrison"}]}]}))))
